# tag::imports[]
from micronaut.coherence.annotation import FilterBinding
# end::imports[]


# tag::clazz[]
@FilterBinding
def Adults(value: str):
    def decorator(target):
        return target
    return decorator
# end::clazz[]
