/*
 * Copyright 2012 Grid Dynamics Consulting Services, Inc.
 *      http://www.griddynamics.com
 *
 * Copyright 2013 Jakub Jirutka <jakub@jirutka.cz>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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
package com.griddynamics.banshun;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.TargetSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.atomic.AtomicReference;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public class LookupTargetSource implements TargetSource {

    private static final Logger log = LoggerFactory.getLogger(LookupTargetSource.class);

    private AtomicReference<Object> target = new AtomicReference<>();

    private final String serviceName;
    private final Class<?> serviceInterface;
    private final String exportProxyName;
    private final ApplicationContext rootContext;


    public LookupTargetSource(String serviceName, Class<?> serviceInterface, String exportProxyName, ApplicationContext rootContext) {
        this.serviceName = serviceName;
        this.serviceInterface = serviceInterface;
        this.exportProxyName = exportProxyName;
        this.rootContext = rootContext;
    }


    public Class<?> getTargetClass() {
        return serviceInterface;
    }

    public boolean isStatic() {
        return false;
    }

    public void releaseTarget(Object target) throws Exception {
    }

    public Object getTarget() throws BeansException {
        Object localTarget = target.get();

        if (localTarget == null) {
            if (!rootContext.containsBean(exportProxyName)) {
                throw new NoSuchBeanDefinitionException(exportProxyName, String.format(
                        "can't find export declaration for lookup(%s, %s)", serviceName, serviceInterface));
            }
            ExportTargetSource exportProxy = rootContext.getBean(exportProxyName, ExportTargetSource.class);

            // verify if service interfaces on both sides are compatible
            if (!serviceInterface.isAssignableFrom(exportProxy.getTargetClass())) {
                throw new BeanNotOfRequiredTypeException(serviceName, serviceInterface, exportProxy.getTargetClass());
            }

            if (target.compareAndSet(null, localTarget = exportProxy.getTarget())) {
                return localTarget;

            } else {
                // log potentially redundant instance initialization
                log.warn("Bean {} was created earlier", serviceName);
                return target.get();
            }
        }
        return localTarget;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, SHORT_PREFIX_STYLE);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj, "target");
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, "target");
    }
}
