from abc import ABC, abstractmethod
from typing import Generic, TypeVar

F = TypeVar("F")
T = TypeVar("T")


class Converter(ABC, Generic[F, T]):
    """A service that converts values."""

    @abstractmethod
    def convert(self, value: F) -> T:
        ...
