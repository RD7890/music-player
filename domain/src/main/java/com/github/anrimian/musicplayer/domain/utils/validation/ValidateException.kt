package com.github.anrimian.musicplayer.domain.utils.validation

class ValidateException(val validateErrors: List<ValidateError>) : RuntimeException()
