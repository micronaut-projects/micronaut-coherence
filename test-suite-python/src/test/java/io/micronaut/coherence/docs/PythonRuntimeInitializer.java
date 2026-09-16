package io.micronaut.coherence.docs;

import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.TypeConverter;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.graalvm.polyglot.Context;

import java.util.Optional;

/**
 * TODO(python): the Coherence event listener and topic listener processors are
 * {@code ExecutableMethodProcessor}s created by {@code DefaultBeanContext.processExecutableMethodsProcessAtStartup()}
 * before the {@code @Context} beans (among them the GraalPy runtime) are initialized, so a Python bean instantiated
 * by such a processor would fail with "GraalPy context has not been initialized".
 * <p>
 * {@code TypeConverter} beans are the only beans the application context creates before the startup
 * processors ({@code DefaultApplicationContext.initializeTypeConverters()}), so this no-op converter
 * (an {@code Object -> Object} converter is never registered with the conversion service) injects the
 * GraalPy context to make sure the runtime is installed before the first Python bean is instantiated.
 */
@Singleton
public class PythonRuntimeInitializer implements TypeConverter<Object, Object> {

    public PythonRuntimeInitializer(@Named("python") Context graalPyContext) {
        // injecting the context creates and installs the GraalPy runtime
    }

    @Override
    public Optional<Object> convert(Object object, Class<Object> targetType, ConversionContext context) {
        return Optional.empty();
    }
}
