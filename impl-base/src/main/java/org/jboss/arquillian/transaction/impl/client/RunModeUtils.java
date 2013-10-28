/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.transaction.impl.client;

import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.client.deployment.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;

import java.lang.reflect.Method;

/**
 * Hack to check if this is running on a embedded container.
 * Remove when Arquillian Core support proper separation between client and embedded containers.
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
final class RunModeUtils
{
   private RunModeUtils()
   {
   }

   /**
    * Check is this should run as client.
    * <p/>
    * Verify @Deployment.testable vs @RunAsClient on Class or Method level
    *
    * @param deployment
    * @param testClass
    * @param testMethod
    * @return
    */
   public static boolean isRunAsClient(Deployment deployment, Class<?> testClass, Method testMethod)
   {
      boolean runAsClient = true;
      if (deployment != null)
      {
         runAsClient = deployment.getDescription().testable() ? false : true;
         runAsClient = deployment.isDeployed() ? runAsClient : true;

         if (testMethod.isAnnotationPresent(RunAsClient.class))
         {
            runAsClient = true;
         } else if (testClass.isAnnotationPresent(RunAsClient.class))
         {
            runAsClient = true;
         }
      }
      return runAsClient;
   }

   /**
    * Check if this Container DEFAULTs to the Local protocol.
    * <p/>
    * Hack to get around ARQ-391
    *
    * @param container
    * @return true if DeployableContianer.getDefaultProtocol == Local
    */
   public static boolean isLocalContainer(Container container)
   {
      if (container == null || container.getDeployableContainer() == null
            || container.getDeployableContainer().getDefaultProtocol() == null)
      {
         return false;
      }
      if ("Local".equals(container.getDeployableContainer().getDefaultProtocol().getName()))
      {
         return true;
      }
      return false;
   }
}
