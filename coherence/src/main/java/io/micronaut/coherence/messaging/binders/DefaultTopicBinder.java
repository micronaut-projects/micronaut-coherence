/*
 * Copyright 2017-2021 original authors
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
package io.micronaut.coherence.messaging.binders;

import com.tangosol.net.topic.Position;
import com.tangosol.net.topic.Subscriber;
import com.tangosol.util.Binary;
import io.micronaut.core.bind.ArgumentBinder;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * The default binder that binds the Kafka value for a {@link Subscriber.Element}.
 *
 * @param <T> The target generic type
 * @author Jonathan Knight
 * @since 1.0
 */
public class DefaultTopicBinder<T> implements ElementBinder<T> {

    private static final DefaultTopicBinder<?> INSTANCE = new DefaultTopicBinder<>();

    private final Map<Argument<?>, Function<Subscriber.Element<?>, Object>> defaultResolver;

    /**
     * Default constructor.
     */
    public DefaultTopicBinder() {
        this.defaultResolver = new HashMap<>();
        Function<Subscriber.Element<?>, Object> channelFunc = Subscriber.Element::getChannel;
        Function<Subscriber.Element<?>, Object> positionFunc = Subscriber.Element::getPosition;
        Function<Subscriber.Element<?>, Object> timestampFunc = Subscriber.Element::getTimestamp;
        Function<Subscriber.Element<?>, Object> timestampLongFunc = e -> e.getTimestamp().toEpochMilli();

        this.defaultResolver.put(
                Argument.of(Integer.class, "channel"),
                channelFunc
        );
        this.defaultResolver.put(
                Argument.of(Position.class, "position"),
                positionFunc
        );
        this.defaultResolver.put(
                Argument.of(Instant.class, "timestamp"),
                timestampFunc
        );
        this.defaultResolver.put(
                Argument.of(Long.class, "timestamp"),
                timestampLongFunc
        );

        this.defaultResolver.put(
                Argument.of(int.class, "channel"),
                channelFunc
        );
        this.defaultResolver.put(
                Argument.of(long.class, "timestamp"),
                timestampLongFunc
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public ArgumentBinder.BindingResult<T> bind(ArgumentConversionContext<T> context, Subscriber.Element<?> element) {
        Argument<T> argument = context.getArgument();
        Function<Subscriber.Element<?>, Object> f = defaultResolver.get(argument);
        if (f != null) {
            Optional<Object> opt = Optional.of(f.apply(element));
            return () -> (Optional<T>) opt;
        } else if (argument.getType() == Subscriber.Element.class) {
            Optional<? extends Subscriber.Element<?>> opt = Optional.of(element);
            //noinspection unchecked
            return () -> (Optional<T>) opt;
        } else if (argument.getType() == Binary.class) {
            Optional<? extends Binary> opt = Optional.of(element.getBinaryValue());
            //noinspection unchecked
            return () -> (Optional<T>) opt;
        } else {
            Object value = element.getValue();
            Optional<T> converted = ConversionService.SHARED.convert(value, context);
            return () -> converted;
        }
    }

    /**
     * Returns the singleton {@link DefaultTopicBinder} instance.
     * @param <T> the argument type
     * @return the singleton {@link DefaultTopicBinder} instance
     */
    @SuppressWarnings("unchecked")
    public static <T> DefaultTopicBinder<T> instance() {
        return (DefaultTopicBinder<T>) INSTANCE;
    }
}
