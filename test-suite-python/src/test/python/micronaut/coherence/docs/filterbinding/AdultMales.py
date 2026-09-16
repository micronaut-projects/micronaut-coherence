# tag::imports[]
from micronaut.coherence.annotation import FilterBinding
# end::imports[]


# tag::clazz[]
@FilterBinding                         # <1>
def AdultMales():                      # <2>
    def decorator(target):
        return target
    return decorator
# end::clazz[]
