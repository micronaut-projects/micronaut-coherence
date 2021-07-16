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

import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.TypeConverterRegistrar;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


/**
 * Converter registrar for reactive types.
 *
 * @author Vaso Putica
 * @since 2.0
 */
@Singleton
@Requires(classes = Flux.class)
@BootstrapContextCompatible
public class ReactiveConversionService implements TypeConverterRegistrar {

    @Override
    public void register(ConversionService<?> conversionService) {
        conversionService.addConverter(Mono.class, Publisher.class, Mono::flux);
        conversionService.addConverter(Object.class, Publisher.class, Mono::just);

        conversionService.addConverter(Flux.class, Mono.class, source -> source.take(1, true).single());
        conversionService.addConverter(Object.class, Flux.class, o -> {
            if (o instanceof Iterable) {
                return Flux.fromIterable((Iterable) o);
            } else {
                return Flux.just(o);
            }
        });

        conversionService.addConverter(Publisher.class, Flux.class, (publisher) -> {
            if (publisher instanceof Flux) {
                return (Flux) publisher;
            }
            return Flux.from(publisher);
        });
        conversionService.addConverter(Publisher.class, Mono.class, Mono::from);
    }
}
