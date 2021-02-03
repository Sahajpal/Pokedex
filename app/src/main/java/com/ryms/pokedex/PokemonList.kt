package com.ryms.pokedex

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mancj.materialsearchbar.MaterialSearchBar
import com.ryms.pokedex.Adapter.PokemonListAdapter
import com.ryms.pokedex.Common.Common
import com.ryms.pokedex.Common.ItemOffsetDecoration
import com.ryms.pokedex.Model.Pokemon
import com.ryms.pokedex.Retrofit.IPokemonList
import com.ryms.pokedex.Retrofit.RetrofitClient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit

class PokemonList : Fragment() {

    internal var compositeDisposable = CompositeDisposable()
    internal var iPokemonList: IPokemonList

    internal lateinit var recyclerView: RecyclerView

    internal lateinit var search_bar: MaterialSearchBar

    internal lateinit var adapter: PokemonListAdapter
    internal lateinit var search_adapter: PokemonListAdapter
    internal var last_suggest: MutableList<String> = ArrayList()

    init {
        var retrofit: Retrofit = RetrofitClient.instance
        iPokemonList = retrofit.create(IPokemonList::class.java)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val itemView = inflater.inflate(R.layout.fragment_pokemon_list, container, false)

        recyclerView = itemView.findViewById(R.id.pokemon_recyclerview) as RecyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = GridLayoutManager(activity, 2)
        val itemDecoration = ItemOffsetDecoration(activity!!, R.dimen.spacing)
        recyclerView.addItemDecoration(itemDecoration)

        search_bar = itemView.findViewById(R.id.search_bar) as MaterialSearchBar
        search_bar.addTextChangeListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val suggest = ArrayList<String>()
                for(search in last_suggest)
                    if(search.toLowerCase().contains(search_bar.text.toLowerCase()))
                        suggest.add(search)
                search_bar.lastSuggestions = suggest
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })
        search_bar.setOnSearchActionListener(object: MaterialSearchBar.OnSearchActionListener{
            override fun onSearchStateChanged(enabled: Boolean) {
                if (!enabled)
                    recyclerView.adapter = adapter
            }

            override fun onSearchConfirmed(text: CharSequence?) {
                startSearch(text.toString())
            }

            override fun onButtonClicked(buttonCode: Int) {
            }
        })

        fetchData()
        return itemView
    }

    private fun startSearch(text: String) {
        if(Common.pokemonList.size > 0) {
            val result = ArrayList<Pokemon>()
            for (pokemon in Common.pokemonList)
                if (pokemon.name!!.toLowerCase().contains(text.toLowerCase()))
                    result.add(pokemon)
            search_adapter = PokemonListAdapter(activity!!, result)
            recyclerView.adapter = search_adapter
        }
    }

    private fun fetchData() {
        compositeDisposable.add(iPokemonList.listPokemon
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {pokemonDex ->
                Common.pokemonList = pokemonDex.pokemon!!
                adapter = PokemonListAdapter(activity!!, Common.pokemonList)
                recyclerView.adapter = adapter

                last_suggest.clear()
                for (pokemon in Common.pokemonList)
                    last_suggest.add(pokemon.name!!)
                search_bar.visibility = View.VISIBLE
                search_bar.lastSuggestions = last_suggest
            }
        );
    }
}