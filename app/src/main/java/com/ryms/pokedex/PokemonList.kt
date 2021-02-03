package com.ryms.pokedex

import android.content.Intent
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

    internal lateinit var searchBar: MaterialSearchBar

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

        searchBar.setHint("Enter Pokemon Name")
        searchBar.setCardViewElevation(10)
        searchBar.addTextChangeListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val suggest = ArrayList<String>()
                for(search: in last_suggest)
                    if(search.toLowerCase().contains(searchBar.text.toLowerCase()))
                        suggest.add(search)
                searchBar.lastSuggestions = suggest
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })
        searchBar.setOnSearchActionListener(object: MaterialSearchBar.OnSearchActionListener{
            override fun onSearchStateChanged(enabled: Boolean) {
            }

            override fun onSearchConfirmed(text: CharSequence?) {
            }

            override fun onButtonClicked(buttonCode: Int) {
            }
        })

        fetchData()
        return itemView
    }

    private fun fetchData() {
        compositeDisposable.add(iPokemonList.listPokemon
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {pokemonDex ->
                Common.pokemonList = pokemonDex.pokemon!!
                adapter = PokemonListAdapter(activity!!, Common.pokemonList)
                recyclerView.adapter = adapter
            }
        );
    }

}