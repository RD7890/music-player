package com.github.anrimian.musicplayer.domain.utils.validation

abstract class SingleFieldValidator<T: Any> : Validator<T>() {

    override fun validateModel(model: T, outValidateErrors: MutableList<ValidateError>) {
        val error = validateModel(model)
        if (error != null) {
            outValidateErrors.add(error)
        }
    }

    protected abstract fun validateModel(model: T): ValidateError?

}
