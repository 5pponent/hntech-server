package hntech.hntechserver.utils.error

import org.springframework.validation.BindingResult

class ValidationException(var bindingResult: BindingResult): RuntimeException("validation error")