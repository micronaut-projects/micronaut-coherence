package io.micronaut.coherence.docs;

import io.micronaut.context.python.PythonContextRuntime;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.TypeConverter;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.graalvm.polyglot.Context;

import java.util.Optional;

/**
 * TODO(python): the {@code @CoherenceTopicListener} beans are instantiated on the Coherence thread when the
 * Coherence started event is dispatched, at the same time as the JUnit thread instantiates the test class of the
 * same Python package. The first import of two modules of a package that is not imported yet, on two threads,
 * deadlocks in the Python import system ("_DeadlockError: deadlock detected by
 * _ModuleLock('micronaut.coherence.docs.messaging.MessagingTest')"): each thread holds the lock of its module and
 * waits for the package, whose initialiser imports the module the other thread holds. Importing the package on
 * the startup thread first avoids the race.
 * <p>
 * {@code TypeConverter} beans are created before the startup processors ({@code DefaultApplicationContext.initializeTypeConverters()}),
 * so this no-op converter (an {@code Object -> Object} converter is never registered with the conversion service)
 * runs early enough.
 */
@Singleton
public class PythonPackageInitializer implements TypeConverter<Object, Object> {

    public PythonPackageInitializer(@Named("python") Context graalPyContext) {
        PythonContextRuntime.helper(graalPyContext, "__micronaut_import_module").execute("micronaut.coherence.docs.messaging");
    }

    @Override
    public Optional<Object> convert(Object object, Class<Object> targetType, ConversionContext context) {
        return Optional.empty();
    }
}
