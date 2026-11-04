package com.github.anrimian.musicplayer.domain.utils.validation

import io.reactivex.rxjava3.core.Single
import java.util.LinkedList

abstract class Validator<Model : Any> {

    fun <T : Model> validate(model: T): Single<T> {
        return Single.create { emitter ->
            val errors = LinkedList<ValidateError>()
            validateModel(model, errors)
            if (errors.isEmpty()) {
                emitter.onSuccess(model)
            } else {
                emitter.onError(ValidateException(errors))
            }
        }
    }

    protected abstract fun validateModel(
        model: Model,
        outValidateErrors: MutableList<ValidateError>,
    )

}
