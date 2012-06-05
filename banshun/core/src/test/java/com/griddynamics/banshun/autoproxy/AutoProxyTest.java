/**
 *    Copyright 2012 Grid Dynamics Consulting Services, Inc, All Rights Reserved
 *    http://www.griddynamics.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 *  @Project: Banshun
 * */
package com.griddynamics.banshun.autoproxy;

import com.griddynamics.banshun.Registry;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class AutoProxyTest {
    @Test
    public void testAutoProxy() {
        ApplicationContext context = new ClassPathXmlApplicationContext("com/griddynamics/banshun/autoproxy/root-context.xml");
        Registry registry = (Registry) context.getBean("root");
        CustomerService customer = registry.lookup("customer", CustomerService.class);

        Assert.assertEquals("AroundMethod: Customer Name", customer.getName());
    }
}
