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
package io.micronaut.coherence.annotation;

import java.io.Serial;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Objects;

/**
 * Supports inline instantiation of annotation type instances.
 * <p>
 * An instance of an annotation type may be obtained by subclassing {@code AnnotationLiteral}.
 * <pre>
 * public abstract class PayByQualifier
 *       extends AnnotationLiteral&lt;PayBy&gt;
 *       implements PayBy {}
 * </pre>
 * An extension of AnnotationLiteral must do two things:<OL>
 * <LI>Must have the target annotation as its generic type</LI>
 * <LI>Must implement the target type</LI>
 * </OL>
 * In particular, in-line anonymous extensions of AnnotationLiteral will not work because in-line anonymous extensions
 * of AnnotationLiteral cannot implement the target annotation
 *
 * @param <T> the annotation type
 */
abstract class AnnotationLiteral<T extends Annotation> implements Annotation, Serializable {

    @Serial
    private static final long serialVersionUID = -3645430766814376616L;

    private transient volatile boolean m_fAnnotationTypeChecked;
    private transient Class<T> m_annotationType;
    private transient Method[] m_aMembers;

    protected AnnotationLiteral() {
        Class<?> thisClass = this.getClass();

        boolean foundAnnotation = false;
        for (Class<?> iClass : thisClass.getInterfaces()) {
            if (iClass.isAnnotation()) {
                foundAnnotation = true;
                break;
            }
        }

        if (!foundAnnotation) {
            throw new IllegalStateException(
                    "The subclass " + thisClass.getName() + " of AnnotationLiteral must implement an Annotation");
        }
    }

    private static Class<?> getAnnotationLiteralSubclass(Class<?> clazz) {
        Class<?> superclass = clazz.getSuperclass();
        if (superclass.equals(AnnotationLiteral.class)) {
            return clazz;
        } else if (superclass.equals(Object.class)) {
            return null;
        } else {
            return getAnnotationLiteralSubclass(superclass);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> getTypeParameter(Class<?> annotationLiteralSuperclass) {
        Type type = annotationLiteralSuperclass.getGenericSuperclass();
        if (type instanceof ParameterizedType parameterizedType) {
            if (parameterizedType.getActualTypeArguments().length == 1) {
                Object result = parameterizedType.getActualTypeArguments()[0];
                if (Class.class.equals(result)) {
                    return (Class<T>) parameterizedType.getActualTypeArguments()[0];
                }
            }
        }
        return null;
    }

    private static void setAccessible(final AccessibleObject ao) {
        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            ao.setAccessible(true);
            return null;
        });
    }

    private static Object invoke(Method method, Object instance) {
        try {
            if (!method.canAccess(instance)) {
                setAccessible(method);
            }
            return method.invoke(instance);
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(
                    "Error checking value of member method " + method.getName() + " on " + method.getDeclaringClass(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private Method[] getMembers() {
        if (m_aMembers == null) {
            Class<T> annotationType = (Class<T>) annotationType();
            if (annotationType != null) {
                m_aMembers = AccessController.doPrivileged((PrivilegedAction<Method[]>) annotationType()::getDeclaredMethods);

                if (m_aMembers.length > 0 && !annotationType().isAssignableFrom(this.getClass())) {
                    throw new RuntimeException(
                            getClass() + " does not implement the annotation type with members " + annotationType().getName());
                }
            } else {
                m_aMembers = new Method[0];
            }
        }
        return m_aMembers;
    }

    /**
     * Method returns the type of the annotation literal. The value is resolved lazily during the first call of this
     * method.
     *
     * @return annotation type of this literal.
     */
    public Class<? extends Annotation> annotationType() {
        if (!m_fAnnotationTypeChecked) {
            m_fAnnotationTypeChecked = true;
            Class<?> annotationLiteralSubclass = getAnnotationLiteralSubclass(this.getClass());
            if (annotationLiteralSubclass == null) {
                throw new RuntimeException(getClass() + "is not a subclass of AnnotationLiteral");
            }
            m_annotationType = getTypeParameter(annotationLiteralSubclass);
        }
        return m_annotationType;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Annotation that) {
            if (Objects.equals(this.annotationType(), that.annotationType())) {
                for (Method member : getMembers()) {
                    Object thisValue = invoke(member, this);
                    Object thatValue = invoke(member, that);
                    if (thisValue instanceof byte[] && thatValue instanceof byte[]) {
                        if (!Arrays.equals((byte[]) thisValue, (byte[]) thatValue)) {
                            return false;
                        }
                    } else if (thisValue instanceof short[] && thatValue instanceof short[]) {
                        if (!Arrays.equals((short[]) thisValue, (short[]) thatValue)) {
                            return false;
                        }
                    } else if (thisValue instanceof int[] && thatValue instanceof int[]) {
                        if (!Arrays.equals((int[]) thisValue, (int[]) thatValue)) {
                            return false;
                        }
                    } else if (thisValue instanceof long[] && thatValue instanceof long[]) {
                        if (!Arrays.equals((long[]) thisValue, (long[]) thatValue)) {
                            return false;
                        }
                    } else if (thisValue instanceof float[] && thatValue instanceof float[]) {
                        if (!Arrays.equals((float[]) thisValue, (float[]) thatValue)) {
                            return false;
                        }
                    } else if (thisValue instanceof double[] && thatValue instanceof double[]) {
                        if (!Arrays.equals((double[]) thisValue, (double[]) thatValue)) {
                            return false;
                        }
                    } else if (thisValue instanceof char[] && thatValue instanceof char[]) {
                        if (!Arrays.equals((char[]) thisValue, (char[]) thatValue)) {
                            return false;
                        }
                    } else if (thisValue instanceof boolean[] && thatValue instanceof boolean[]) {
                        if (!Arrays.equals((boolean[]) thisValue, (boolean[]) thatValue)) {
                            return false;
                        }
                    } else if (thisValue instanceof Object[] && thatValue instanceof Object[]) {
                        if (!Arrays.equals((Object[]) thisValue, (Object[]) thatValue)) {
                            return false;
                        }
                    } else {
                        if (!thisValue.equals(thatValue)) {
                            return false;
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {

        int hashCode = 0;
        for (Method member : getMembers()) {
            int memberNameHashCode = 127 * member.getName().hashCode();
            Object value = invoke(member, this);
            int memberValueHashCode;
            if (value instanceof boolean[]) {
                memberValueHashCode = Arrays.hashCode((boolean[]) value);
            } else if (value instanceof short[]) {
                memberValueHashCode = Arrays.hashCode((short[]) value);
            } else if (value instanceof int[]) {
                memberValueHashCode = Arrays.hashCode((int[]) value);
            } else if (value instanceof long[]) {
                memberValueHashCode = Arrays.hashCode((long[]) value);
            } else if (value instanceof float[]) {
                memberValueHashCode = Arrays.hashCode((float[]) value);
            } else if (value instanceof double[]) {
                memberValueHashCode = Arrays.hashCode((double[]) value);
            } else if (value instanceof byte[]) {
                memberValueHashCode = Arrays.hashCode((byte[]) value);
            } else if (value instanceof char[]) {
                memberValueHashCode = Arrays.hashCode((char[]) value);
            } else if (value instanceof Object[]) {
                memberValueHashCode = Arrays.hashCode((Object[]) value);
            } else {
                memberValueHashCode = value.hashCode();
            }
            hashCode += memberNameHashCode ^ memberValueHashCode;
        }
        return hashCode;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface NoType {
    }
}
