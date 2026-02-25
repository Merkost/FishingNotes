package com.mobileprism.fishing.domain.entity.weather

enum class PressureValues {
    Pa, Bar, mmHg, Psi, Hpa;

    fun getPressureFromHpa(hPa: Int): String {
        return when (this) {
            Pa -> (hPa * 100).toString()
            Bar -> (hPa / 1000).toString()
            mmHg -> (hPa * 0.75006375541921).toInt().toString()
            Psi -> (hPa * 0.0145037738f).let { "%.5g".format(it) }
            Hpa -> hPa.toString()
        }
    }

    fun getPressureFromMmhg(mmHg: Int): String {
        return when (this) {
            Pa -> (mmHg * 133.322).toString()
            Bar -> (mmHg * 0.00133322f).toString()
            this.mmHg -> mmHg.toString()
            Psi -> (mmHg * 0.0193368f).let { "%.5g".format(it) }
            Hpa -> (mmHg * 1.33).toString()
        }
    }

    fun getPressureMmhg(value: Double): Int {
        return when (this) {
            Pa -> (value * 0.0075006156130264f).toInt()
            Bar -> (value * 750.06168f).toInt()
            mmHg -> value.toInt()
            Psi -> (value * 51.71484f).toInt()
            Hpa -> (value * 1.33f).toInt()
        }
    }
}

enum class TemperatureValues {
    C, F, K;

    fun getTemperature(temperature: Float): String {
        return when (this) {
            C -> temperature.toInt().toString()
            F -> (temperature * 9f / 5f + 32).toInt().toString()
            K -> (temperature + 273.15).toInt().toString()
        }
    }

    fun getDefaultTemperature(temperature: Double): Int {
        return when (this) {
            C -> temperature.toInt()
            F -> ((temperature - 32) * (5 / 9)).toInt()
            K -> (temperature - 273.15).toInt()
        }
    }
}

enum class WindSpeedValues {
    metersps, milesph, knots, ftps, kmph;

    fun getWindSpeed(windSpeed: Double): String {
        val value = when (this) {
            metersps -> windSpeed
            knots -> (windSpeed * 1.9438444924574)
            milesph -> (windSpeed * 2.2369362920544)
            ftps -> (windSpeed * 3.28084)
            kmph -> (windSpeed * 3.6)
        }
        return ((value * 10).toInt() / 10.0).toString()
    }

    fun getDefaultWindSpeed(windSpeed: Double): String {
        return (when (this) {
            metersps -> windSpeed
            knots -> (windSpeed * 1.9438444924574)
            milesph -> (windSpeed * 2.2369362920544)
            ftps -> (windSpeed * 3.28084)
            kmph -> (windSpeed * 3.6)
        }).toInt().toString()
    }
}
