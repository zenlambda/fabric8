/**
 *  Copyright 2005-2014 Red Hat, Inc.
 *
 *  Red Hat licenses this file to you under the Apache License, version
 *  2.0 (the "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.  See the License for the specific language governing
 *  permissions and limitations under the License.
 */
package io.fabric8.container.process;

import org.apache.curator.CuratorZookeeperClient;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CreateBuilder;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.framework.api.DeleteBuilder;
import org.apache.curator.framework.api.ExistsBuilder;
import org.apache.curator.framework.api.GetACLBuilder;
import org.apache.curator.framework.api.GetChildrenBuilder;
import org.apache.curator.framework.api.GetDataBuilder;
import org.apache.curator.framework.api.SetACLBuilder;
import org.apache.curator.framework.api.SetDataBuilder;
import org.apache.curator.framework.api.SyncBuilder;
import org.apache.curator.framework.api.UnhandledErrorListener;
import org.apache.curator.framework.api.transaction.CuratorTransaction;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.listen.Listenable;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.utils.EnsurePath;
import org.apache.zookeeper.Watcher;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.fabric8.container.process.JolokiaAgentHelper.substituteVariableExpression;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class JolokiaAgentHelperSubstituteTest {

    private CuratorFramework curator;

    @Test
    public void testExpressions() throws Exception {
        System.setProperty("CHEESE", "Edam");

        String JAVA_HOME = System.getenv("JAVA_HOME");
        assertExpression("${env:JAVA_HOME}", JAVA_HOME);
        if (JAVA_HOME != null) {
            assertExpression("A${env:JAVA_HOME}B", "A" + JAVA_HOME + "B");
            assertExpression("A${env:JAVA_HOME?:DEFAULT}B", "A" + JAVA_HOME + "B");
        }
        assertExpression("${env:DOES_NOT_EXIST?:DEFAULT}", "DEFAULT");
        assertExpression("A${env:DOES_NOT_EXIST?:DEFAULT}B", "ADEFAULTB");
        assertExpression("${env:DOES_NOT_EXIST?:DEFAULT}${env:DOES_NOT_EXIST?:DEFAULT}", "DEFAULTDEFAULT");
        assertExpression("1${env:DOES_NOT_EXIST?:DEFAULT}2${env:DOES_NOT_EXIST?:DEFAULT}3", "1DEFAULT2DEFAULT3");

    }

    @Test
    public void testHttpUrlExpressions() throws Exception {
        Map<String, String> envVars = new HashMap<>();
        String expression = "http://${env:FABRIC8_LISTEN_ADDRESS}:${env:FABRIC8_HTTP_PROXY_PORT}/";
        assertExpression(expression, expression, true, envVars, curator);

        assertExpression(expression, "http://:/", false, envVars, curator);

        envVars.put("FABRIC8_LISTEN_ADDRESS", "localhost");
        envVars.put("FABRIC8_HTTP_PROXY_PORT", "8181");
        assertExpression(expression, "http://localhost:8181/", true, envVars, curator);
    }

    @Test
    public void testHttpUrlExpressionsWithCurator() throws Exception {
        curator = new MockCuratorFramework();
        testHttpUrlExpressions();
    }


    @Test
    public void testPreserveUnresolved() throws Exception {
        String[] expressions = {
                "${env:DOES_NOT_EXIST?:DEFAULT}",
                "A${env:DOES_NOT_EXIST?:DEFAULT}B",
                "${env:DOES_NOT_EXIST?:DEFAULT}${env:DOES_NOT_EXIST?:DEFAULT}",
                "1${env:DOES_NOT_EXIST?:DEFAULT}2${env:DOES_NOT_EXIST?:DEFAULT}3"

        };
        for (String expression : expressions) {
            assertExpression(expression, expression, true);
        }
    }

    @Test
    public void testInnocentNestedSubstitution() throws Exception {
        curator = mock(CuratorFramework.class);
        GetDataBuilder dataBuilder = mock(GetDataBuilder.class);

        when(dataBuilder.forPath("/fabric/registry/clusters/foo/myfoo/master"))
                .thenReturn("foo0".getBytes());
        when(dataBuilder.forPath("/fabric/registry/containers/foo0/config/ip"))
                .thenReturn("127.0.0.1".getBytes());
        when(curator.getData()).thenReturn(dataBuilder);

        assertExpression("${zk:/fabric/registry/containers/" +
                "${zk:/fabric/registry/clusters/foo/myfoo/master}" +
                "/config/ip}", "127.0.0.1");

    }

    @Test
    public void testEvilNestedZKSubsitution() throws Exception {
        curator = mock(CuratorFramework.class);
        GetDataBuilder dataBuilder = mock(GetDataBuilder.class);

        when(dataBuilder.forPath("/fabric/registry/clusters/foo/myfoo/master"))
                .thenReturn("}PICKLE".getBytes()); // evil bit... well bytes really...
        when(dataBuilder.forPath("/fabric/registry/containers/}PICKLE/config/ip"))
                .thenReturn("127.0.0.1".getBytes());
        when(curator.getData()).thenReturn(dataBuilder);

        assertExpression("${zk:/fabric/registry/containers/" +
                "${zk:/fabric/registry/clusters/foo/myfoo/master}" +
                "/config/ip}", "127.0.0.1");
    }

    @Test
    public void testNestedZKSubstitutionWithJsonNode() throws Exception {
        curator = mock(CuratorFramework.class);
        GetDataBuilder dataBuilder = mock(GetDataBuilder.class);
        // perhaps not real use case, but nodes are likely to spit out json
        // so you don't want it to fail weird
        when(dataBuilder.forPath("/fabric/registry/clusters/foo/myfoo/master"))
                .thenReturn("{ \"id\" : \"foo\", \"services\" : [ \"bar\", \"baz\"]}".getBytes());
        when(dataBuilder.forPath("/fabric/registry/containers/" +
                "{ \"id\" : \"foo\", \"services\" : [ \"bar\", \"baz\"]}/config/ip"))
                .thenReturn("127.0.0.1".getBytes());
        when(curator.getData()).thenReturn(dataBuilder);

        assertExpression("${zk:/fabric/registry/containers/" +
                "${zk:/fabric/registry/clusters/foo/myfoo/master}" +
                "/config/ip}", "127.0.0.1");
    }

    @Test
    public void testGroovySubstitutionNestedInsideZKSubstitution() throws Exception {
        curator = mock(CuratorFramework.class);
        GetDataBuilder dataBuilder = mock(GetDataBuilder.class);
        when(dataBuilder.forPath("/fabric/registry/containers/config/cheese/ip"))
                .thenReturn("127.0.0.1".getBytes());
        when(curator.getData()).thenReturn(dataBuilder);

        assertExpression("${zk:/fabric/registry/containers/config/" +
                "${groovy:'CHEESE'.toLowerCase()}/ip}", "127.0.0.1");

    }

    @Test
    public void testGroovySubstitutionWithUnmatchedOpenBraceNestedInsideZKSubstitution() throws Exception {
        curator = mock(CuratorFramework.class);
        GetDataBuilder dataBuilder = mock(GetDataBuilder.class);
        when(dataBuilder.forPath("/fabric/registry/containers/config/cheese/ip"))
                .thenReturn("127.0.0.1".getBytes());
        when(curator.getData()).thenReturn(dataBuilder);

        assertExpression("${zk:/fabric/registry/containers/config/" +
                "${groovy:'{cheese'.substring(1)}/ip}", "127.0.0.1");

    }

    @Test
    public void testGroovySubstitutionWithUnmatchedCloseBraceNestedInsideZKSubstitution() throws Exception {
        curator = mock(CuratorFramework.class);
        GetDataBuilder dataBuilder = mock(GetDataBuilder.class);
        when(dataBuilder.forPath("/fabric/registry/containers/config/pickle/ip"))
                .thenReturn("127.0.0.1".getBytes());
        when(curator.getData()).thenReturn(dataBuilder);

        assertExpression("${zk:/fabric/registry/containers/config/" +
                "${groovy:'}pickle'.substring(1)}/ip}", "127.0.0.1");

    }

    @Test
    public void testGroovySubstitutionWithNestedInsideZKSubstitutionBracesBecomeUnbalanced() throws Exception {
        curator = mock(CuratorFramework.class);
        GetDataBuilder dataBuilder = mock(GetDataBuilder.class);
        when(dataBuilder.forPath("/fabric/registry/containers/config/pickle/ip"))
                .thenReturn("127.0.0.1".getBytes());
        when(curator.getData()).thenReturn(dataBuilder);

        assertExpression("${zk:/fabric/registry/containers/config/" +
                "${groovy:'{pickle}'.substring(1)}/ip}", "127.0.0.1");

    }

    @Test
    public void testGroovySubstitutionWithClosureNestedInsideZKSubstitution() throws Exception {
        curator = mock(CuratorFramework.class);
        GetDataBuilder dataBuilder = mock(GetDataBuilder.class);
        when(dataBuilder.forPath("/fabric/registry/containers/config/pickle/ip"))
                .thenReturn("127.0.0.1".getBytes());
        when(curator.getData()).thenReturn(dataBuilder);

        assertExpression("${zk:/fabric/registry/containers/config/" +
                "${groovy:'PICKLE'.collect { it.toLowerCase() }.join('')}/ip}", "127.0.0.1");

    }

    @Test
    public void testZKSubstitutionNestedInsideGroovySubstitution() throws Exception {
        // evaluate zk while building a groovy expression... urgh!
        // suppose this might be useful if you want to load a function name
        // or even groovy code from ZK... but you'd have to be a bit crazy...
        curator = mock(CuratorFramework.class);
        GetDataBuilder dataBuilder = mock(GetDataBuilder.class);
        when(dataBuilder.forPath("/fabric/registry/containers/config/root/ip"))
                .thenReturn("PICKLE".getBytes());
        when(curator.getData()).thenReturn(dataBuilder);

        assertExpression("${groovy:'${zk:/fabric/registry/containers/config/root/ip}'.equals('PICKLE')}","true");
    }




    @Test
    public void testGroovySubstitution() {
        curator = new MockCuratorFramework();
//        assertExpression("${groovy:([1,2,3,4])}","[1, 2, 3, 4]",false);
//        assertExpression("${groovy:([1,2,3,4].findAll { it % 2 == 0 \\})}","[2, 4]",false);
        assertExpression("${groovy:([1,2,3,4].findAll { it % 2 == 0 })}","[2, 4]",false);

    }

    @Test
    public void testProvidedEnvironmentVar() throws Exception {
        Map<String, String> vars = new HashMap<>();
        vars.put("CHEESE", "Edam");

        assertExpression("${env:CHEESE}", "Edam", false, vars, curator);
        assertExpression("A${env:CHEESE}B", "A" + "Edam" + "B", false, vars, curator);
        assertExpression("A${env:CHEESE?:DEFAULT}B", "A" + "Edam" + "B", false, vars, curator);

        assertExpression("${env:DOES_NOT_EXIST?:DEFAULT}", "DEFAULT", false, vars, curator);
        assertExpression("A${env:DOES_NOT_EXIST?:DEFAULT}B", "ADEFAULTB", false, vars, curator);
        assertExpression("${env:DOES_NOT_EXIST?:DEFAULT}${env:DOES_NOT_EXIST?:DEFAULT}", "DEFAULTDEFAULT", false, vars, curator);
        assertExpression("1${env:DOES_NOT_EXIST?:DEFAULT}2${env:DOES_NOT_EXIST?:DEFAULT}3", "1DEFAULT2DEFAULT3", false, vars, curator);
    }

    public String assertExpression(String expression, String expectedValue) {
        return assertExpression(expression, expectedValue, false);
    }

    public String assertExpression(String expression, String expectedValue, boolean preserveUnresolved) {
        return assertExpression(expression, expectedValue, preserveUnresolved, System.getenv(), curator);
    }

    public static String assertExpression(String expression, String expectedValue, boolean preserveUnresolved, Map<String, String> envVars, CuratorFramework curator) {
        String actual = substituteVariableExpression(expression, envVars, null, curator, preserveUnresolved);
        System.out.println("expression> " + expression + " => " + actual);
        assertEquals("Expression " + expression, expectedValue, actual);
        return actual;
    }

    private static class MockCuratorFramework implements CuratorFramework {
        @Override
        public void start() {
        }

        @Override
        public void close() {
        }

        @Override
        public CuratorFrameworkState getState() {
            return null;
        }

        @Override
        public boolean isStarted() {
            return false;
        }

        @Override
        public CreateBuilder create() {
            return null;
        }

        @Override
        public DeleteBuilder delete() {
            return null;
        }

        @Override
        public ExistsBuilder checkExists() {
            return null;
        }

        @Override
        public GetDataBuilder getData() {
            return null;
        }

        @Override
        public SetDataBuilder setData() {
            return null;
        }

        @Override
        public GetChildrenBuilder getChildren() {
            return null;
        }

        @Override
        public GetACLBuilder getACL() {
            return null;
        }

        @Override
        public SetACLBuilder setACL() {
            return null;
        }

        @Override
        public CuratorTransaction inTransaction() {
            return null;
        }

        @Override
        public void sync(String s, Object o) {
        }

        @Override
        public SyncBuilder sync() {
            return null;
        }

        @Override
        public Listenable<ConnectionStateListener> getConnectionStateListenable() {
            return null;
        }

        @Override
        public Listenable<CuratorListener> getCuratorListenable() {
            return null;
        }

        @Override
        public Listenable<UnhandledErrorListener> getUnhandledErrorListenable() {
            return null;
        }

        @Override
        public CuratorFramework nonNamespaceView() {
            return null;
        }

        @Override
        public CuratorFramework usingNamespace(String s) {
            return null;
        }

        @Override
        public String getNamespace() {
            return null;
        }

        @Override
        public CuratorZookeeperClient getZookeeperClient() {
            return null;
        }

        @Override
        public EnsurePath newNamespaceAwareEnsurePath(String s) {
            return null;
        }

        @Override
        public void clearWatcherReferences(Watcher watcher) {
        }

        @Override
        public boolean blockUntilConnected(int i, TimeUnit timeUnit) throws InterruptedException {
            return false;
        }

        @Override
        public void blockUntilConnected() throws InterruptedException {
        }
    }
}
