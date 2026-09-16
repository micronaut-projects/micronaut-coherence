package io.micronaut.coherence.docs;

import io.micronaut.context.python.PythonContextRuntime;
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
 * <p>
 * TODO(python): the Coherence lifecycle events are dispatched on several {@code ForkJoinPool} threads at once and
 * every one of them instantiates a Python listener bean; the concurrent first load of the {@code micronaut_runtime}
 * helper module leaves it partially initialized ("The micronaut_runtime module does not define
 * [__micronaut_import_module]"), so a helper is resolved eagerly on the startup thread to load the module first.
 */
@Singleton
public class PythonRuntimeInitializer implements TypeConverter<Object, Object> {

    public PythonRuntimeInitializer(@Named("python") Context graalPyContext) {
        // injecting the context creates and installs the GraalPy runtime
        PythonContextRuntime.helper(graalPyContext, "__micronaut_import_module");
    }

    @Override
    public Optional<Object> convert(Object object, Class<Object> targetType, ConversionContext context) {
        return Optional.empty();
    }
}
