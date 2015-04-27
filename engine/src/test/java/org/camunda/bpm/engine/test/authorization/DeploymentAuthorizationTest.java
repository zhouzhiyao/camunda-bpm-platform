/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.authorization;

import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Permissions.CREATE;
import static org.camunda.bpm.engine.authorization.Permissions.DELETE;
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE;
import static org.camunda.bpm.engine.authorization.Resources.DEPLOYMENT;

import java.io.InputStream;
import java.util.List;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.AuthorizationQuery;
import org.camunda.bpm.engine.impl.AbstractQuery;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentQuery;
import org.camunda.bpm.engine.repository.Resource;

/**
 * @author Roman Smirnov
 *
 */
public class DeploymentAuthorizationTest extends AuthorizationTest {

  protected static final String FIRST_RESOURCE = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml";
  protected static final String SECOND_RESOURCE = "org/camunda/bpm/engine/test/authorization/messageBoundaryEventProcess.bpmn20.xml";

  // query ////////////////////////////////////////////////////////////

  public void testSimpleDeploymentQueryWithoutAuthorization() {
    // given
    String deploymentId = createDeployment(null);

    // when
    DeploymentQuery query = repositoryService.createDeploymentQuery();

    // then
    verifyQueryResults(query, 0);

    deleteDeployment(deploymentId);
  }

  public void testSimpleDeploymentQueryWithReadPermissionOnDeployment() {
    // given
    String deploymentId = createDeployment(null);
    createGrantAuthorization(DEPLOYMENT, deploymentId, userId, READ);

    // when
    DeploymentQuery query = repositoryService.createDeploymentQuery();

    // then
    verifyQueryResults(query, 1);

    deleteDeployment(deploymentId);
  }

  public void testSimpleDeploymentQueryWithReadPermissionOnAnyDeployment() {
    // given
    String deploymentId = createDeployment(null);
    createGrantAuthorization(DEPLOYMENT, ANY, userId, READ);

    // when
    DeploymentQuery query = repositoryService.createDeploymentQuery();

    // then
    verifyQueryResults(query, 1);

    deleteDeployment(deploymentId);
  }

  public void testDeploymentQueryWithoutAuthorization() {
    // given
    String deploymentId1 = createDeployment("first");
    String deploymentId2 = createDeployment("second");

    // when
    DeploymentQuery query = repositoryService.createDeploymentQuery();

    // then
    verifyQueryResults(query, 0);

    deleteDeployment(deploymentId1);
    deleteDeployment(deploymentId2);
  }

  public void testDeploymentQueryWithReadPermissionOnDeployment() {
    // given
    String deploymentId1 = createDeployment("first");
    String deploymentId2 = createDeployment("second");
    createGrantAuthorization(DEPLOYMENT, deploymentId1, userId, READ);

    // when
    DeploymentQuery query = repositoryService.createDeploymentQuery();

    // then
    verifyQueryResults(query, 1);

    deleteDeployment(deploymentId1);
    deleteDeployment(deploymentId2);
  }

  public void testDeploymentQueryWithReadPermissionOnAnyDeployment() {
    // given
    String deploymentId1 = createDeployment("first");
    String deploymentId2 = createDeployment("second");
    createGrantAuthorization(DEPLOYMENT, ANY, userId, READ);

    // when
    DeploymentQuery query = repositoryService.createDeploymentQuery();

    // then
    verifyQueryResults(query, 2);

    deleteDeployment(deploymentId1);
    deleteDeployment(deploymentId2);
  }

  // create deployment ///////////////////////////////////////////////

  public void testCreateDeploymentWithoutAuthoriatzion() {
    // given

    try {
      // when
      repositoryService
          .createDeployment()
          .addClasspathResource(FIRST_RESOURCE)
          .deploy();
      fail("Exception expected: It should not be possible to create a new deployment");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(CREATE.getName(), message);
      assertTextPresent(DEPLOYMENT.resourceName(), message);
    }
  }

  public void testCreateDeployment() {
    // given
    createGrantAuthorization(DEPLOYMENT, ANY, userId, CREATE);

    // when
    Deployment deployment = repositoryService
      .createDeployment()
      .addClasspathResource(FIRST_RESOURCE)
      .deploy();

    // then
    disableAuthorization();
    DeploymentQuery query = repositoryService.createDeploymentQuery();
    verifyQueryResults(query, 1);
    enableAuthorization();

    deleteDeployment(deployment.getId());
  }

  // delete deployment //////////////////////////////////////////////

  public void testDeleteDeploymentWithoutAuthorization() {
    // given
    String deploymentId = createDeployment(null);

    try {
      // when
      repositoryService.deleteDeployment(deploymentId);
      fail("Exception expected: it should not be possible to delete a deployment");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(DELETE.getName(), message);
      assertTextPresent(DEPLOYMENT.resourceName(), message);
    }

    deleteDeployment(deploymentId);
  }

  public void testDeleteDeploymentWithDeletePermissionOnDeployment() {
    // given
    String deploymentId = createDeployment(null);
    createGrantAuthorization(DEPLOYMENT, deploymentId, userId, DELETE);

    // when
    repositoryService.deleteDeployment(deploymentId);

    // then
    disableAuthorization();
    DeploymentQuery query = repositoryService.createDeploymentQuery();
    verifyQueryResults(query, 0);
    enableAuthorization();
  }

  public void testDeleteDeploymentWithDeletePermissionOnAnyDeployment() {
    // given
    String deploymentId = createDeployment(null);
    createGrantAuthorization(DEPLOYMENT, ANY, userId, DELETE);

    // when
    repositoryService.deleteDeployment(deploymentId);

    // then
    disableAuthorization();
    DeploymentQuery query = repositoryService.createDeploymentQuery();
    verifyQueryResults(query, 0);
    enableAuthorization();
  }

  // get deployment resource names //////////////////////////////////

  public void testGetDeploymentResourceNamesWithoutAuthorization() {
    // given
    String deploymentId = createDeployment(null);

    try {
      // when
      repositoryService.getDeploymentResourceNames(deploymentId);
      fail("Exception expected: it should not be possible to retrieve deployment resource names");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(READ.getName(), message);
      assertTextPresent(DEPLOYMENT.resourceName(), message);
    }

    deleteDeployment(deploymentId);
  }

  public void testGetDeploymentResourceNamesWithReadPermissionOnDeployment() {
    // given
    String deploymentId = createDeployment(null);
    createGrantAuthorization(DEPLOYMENT, deploymentId, userId, READ);

    // when
    List<String> names = repositoryService.getDeploymentResourceNames(deploymentId);

    // then
    assertFalse(names.isEmpty());
    assertEquals(2, names.size());
    assertTrue(names.contains(FIRST_RESOURCE));
    assertTrue(names.contains(SECOND_RESOURCE));

    deleteDeployment(deploymentId);
  }

  public void testGetDeploymentResourceNamesWithReadPermissionOnAnyDeployment() {
    // given
    String deploymentId = createDeployment(null);
    createGrantAuthorization(DEPLOYMENT, ANY, userId, READ);

    // when
    List<String> names = repositoryService.getDeploymentResourceNames(deploymentId);

    // then
    assertFalse(names.isEmpty());
    assertEquals(2, names.size());
    assertTrue(names.contains(FIRST_RESOURCE));
    assertTrue(names.contains(SECOND_RESOURCE));

    deleteDeployment(deploymentId);
  }

  // get deployment resources //////////////////////////////////

  public void testGetDeploymentResourcesWithoutAuthorization() {
    // given
    String deploymentId = createDeployment(null);

    try {
      // when
      repositoryService.getDeploymentResources(deploymentId);
      fail("Exception expected: it should not be possible to retrieve deployment resources");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(READ.getName(), message);
      assertTextPresent(DEPLOYMENT.resourceName(), message);
    }

    deleteDeployment(deploymentId);
  }

  public void testGetDeploymentResourcesWithReadPermissionOnDeployment() {
    // given
    String deploymentId = createDeployment(null);
    createGrantAuthorization(DEPLOYMENT, deploymentId, userId, READ);

    // when
    List<Resource> resources = repositoryService.getDeploymentResources(deploymentId);

    // then
    assertFalse(resources.isEmpty());
    assertEquals(2, resources.size());

    deleteDeployment(deploymentId);
  }

  public void testGetDeploymentResourcesWithReadPermissionOnAnyDeployment() {
    // given
    String deploymentId = createDeployment(null);
    createGrantAuthorization(DEPLOYMENT, ANY, userId, READ);

    // when
    List<Resource> resources = repositoryService.getDeploymentResources(deploymentId);

    // then
    assertFalse(resources.isEmpty());
    assertEquals(2, resources.size());

    deleteDeployment(deploymentId);
  }

  // get resource as stream //////////////////////////////////

  public void testGetResourceAsStreamWithoutAuthorization() {
    // given
    String deploymentId = createDeployment(null);

    try {
      // when
      repositoryService.getResourceAsStream(deploymentId, FIRST_RESOURCE);
      fail("Exception expected: it should not be possible to retrieve a resource as stream");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(READ.getName(), message);
      assertTextPresent(DEPLOYMENT.resourceName(), message);
    }

    deleteDeployment(deploymentId);
  }

  public void testGetResourceAsStreamWithReadPermissionOnDeployment() {
    // given
    String deploymentId = createDeployment(null);
    createGrantAuthorization(DEPLOYMENT, deploymentId, userId, READ);

    // when
    InputStream stream = repositoryService.getResourceAsStream(deploymentId, FIRST_RESOURCE);

    // then
    assertNotNull(stream);

    deleteDeployment(deploymentId);
  }

  public void testGetResourceAsStreamWithReadPermissionOnAnyDeployment() {
    // given
    String deploymentId = createDeployment(null);
    createGrantAuthorization(DEPLOYMENT, ANY, userId, READ);

    // when
    InputStream stream = repositoryService.getResourceAsStream(deploymentId, FIRST_RESOURCE);

    // then
    assertNotNull(stream);

    deleteDeployment(deploymentId);
  }

  // get resource as stream by id//////////////////////////////////

  public void testGetResourceAsStreamByIdWithoutAuthorization() {
    // given
    String deploymentId = createDeployment(null);

    disableAuthorization();
    List<Resource> resources = repositoryService.getDeploymentResources(deploymentId);
    enableAuthorization();
    String resourceId = resources.get(0).getId();

    try {
      // when
      repositoryService.getResourceAsStreamById(deploymentId, resourceId);
      fail("Exception expected: it should not be possible to retrieve a resource as stream");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(READ.getName(), message);
      assertTextPresent(DEPLOYMENT.resourceName(), message);
    }

    deleteDeployment(deploymentId);
  }

  public void testGetResourceAsStreamByIdWithReadPermissionOnDeployment() {
    // given
    String deploymentId = createDeployment(null);
    createGrantAuthorization(DEPLOYMENT, deploymentId, userId, READ);

    disableAuthorization();
    List<Resource> resources = repositoryService.getDeploymentResources(deploymentId);
    enableAuthorization();
    String resourceId = resources.get(0).getId();

    // when
    InputStream stream = repositoryService.getResourceAsStreamById(deploymentId, resourceId);

    // then
    assertNotNull(stream);

    deleteDeployment(deploymentId);
  }

  public void testGetResourceAsStreamByIdWithReadPermissionOnAnyDeployment() {
    // given
    String deploymentId = createDeployment(null);
    createGrantAuthorization(DEPLOYMENT, ANY, userId, READ);

    disableAuthorization();
    List<Resource> resources = repositoryService.getDeploymentResources(deploymentId);
    enableAuthorization();
    String resourceId = resources.get(0).getId();

    // when
    InputStream stream = repositoryService.getResourceAsStreamById(deploymentId, resourceId);

    // then
    assertNotNull(stream);

    deleteDeployment(deploymentId);
  }

  // should create authorization /////////////////////////////////////

  public void testCreateAuthorizationOnDeploy() {
    // given
    createGrantAuthorization(DEPLOYMENT, ANY, userId, CREATE);
    Deployment deployment = repositoryService
      .createDeployment()
      .addClasspathResource(FIRST_RESOURCE)
      .deploy();

    // when
    Authorization authorization = authorizationService
      .createAuthorizationQuery()
      .userIdIn(userId)
      .resourceId(deployment.getId())
      .singleResult();

    // then
    assertNotNull(authorization);
    assertTrue(authorization.isPermissionGranted(READ));
    assertTrue(authorization.isPermissionGranted(DELETE));
    assertFalse(authorization.isPermissionGranted(UPDATE));

    deleteDeployment(deployment.getId());
  }

  // clear authorization /////////////////////////////////////

  public void testClearAuthorizationOnDeleteDeployment() {
    // given
    createGrantAuthorization(DEPLOYMENT, ANY, userId, CREATE);
    Deployment deployment = repositoryService
      .createDeployment()
      .addClasspathResource(FIRST_RESOURCE)
      .deploy();

    String deploymentId = deployment.getId();

    AuthorizationQuery query = authorizationService
      .createAuthorizationQuery()
      .userIdIn(userId)
      .resourceId(deploymentId);

    Authorization authorization = query.singleResult();
    assertNotNull(authorization);

    // when
    repositoryService.deleteDeployment(deploymentId);

    authorization = query.singleResult();
    assertNull(authorization);

    deleteDeployment(deploymentId);
  }

  // helper /////////////////////////////////////////////////////////

  protected void verifyQueryResults(DeploymentQuery query, int countExpected) {
    verifyQueryResults((AbstractQuery<?, ?>) query, countExpected);
  }

  protected String createDeployment(String name) {
    return createDeployment(name, FIRST_RESOURCE, SECOND_RESOURCE).getId();
  }

}