/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.coherence;

import java.io.DataInput;
import java.io.DataOutput;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.oracle.coherence.inject.Injectable;

import com.tangosol.io.DefaultSerializer;
import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.Serializer;
import com.tangosol.util.Binary;
import com.tangosol.util.ExternalizableHelper;

import io.micronaut.context.annotation.Prototype;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@MicronautTest(startApplication = false)
class InjectorImplTest {

    @Test
    void shouldInjectAfterDeserialization() {
        BeanOne beanOne = new BeanOne();
        assertThat(beanOne.getBeanTwo(), is(nullValue()));
        Serializer serializer = new DefaultSerializer();
        Binary binary = ExternalizableHelper.toBinary(beanOne, serializer);
        BeanOne result = ExternalizableHelper.fromBinary(binary, serializer);
        assertThat(result.getBeanTwo(), is(notNullValue()));
    }


    @Prototype
    public static class BeanOne implements ExternalizableLite, Injectable {

        @Inject
        BeanTwo beanTwo;

        public BeanTwo getBeanTwo() {
            return beanTwo;
        }

        @Override
        public void readExternal(DataInput in) {
        }

        @Override
        public void writeExternal(DataOutput out) {
        }
    }

    @Singleton
    public static class BeanTwo {
    }
}
