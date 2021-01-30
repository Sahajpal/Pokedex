package com.ryms.pokedex.Retrofit

import com.ryms.pokedex.Model.Pokedex
import io.reactivex.Observable
import retrofit2.http.GET

interface IPokemonList {
    @get:GET("pokemon.json")
    val listPokemon: Observable<Pokedex>
}