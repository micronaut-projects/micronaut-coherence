# tag::imports[]
from micronaut.coherence.annotation import ExtractorBinding
# end::imports[]


# tag::clazz[]
@ExtractorBinding                         # <1>
def PersonAge():                          # <2>
    def decorator(target):
        return target
    return decorator
# end::clazz[]
