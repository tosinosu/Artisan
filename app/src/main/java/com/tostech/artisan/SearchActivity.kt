package com.tostech.artisan

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.MenuItemCompat
import androidx.navigation.ui.AppBarConfiguration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.firebase.ui.database.SnapshotParser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.tostech.artisan.AdapterClasses.SearchRecyclerAdapter2
import com.tostech.artisan.data.AdvertData
import com.tostech.artisan.data.SearchData

class SearchActivity :  AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var searchRecyclerAdapter: SearchRecyclerAdapter2
    private var mUsers: List<AdvertData>? = null
    private lateinit var userRef: DatabaseReference
    var firebaseUser: FirebaseUser? = null
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var searchArray: ArrayList<SearchData>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

       // val toolbar: Toolbar = findViewById(R.id.toolbarSearch)
        //toolbar.setTitleTextColor(Color.WHITE)
//        setSupportActionBar(toolbar)


        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)


       // supportActionBar?.title = "Search"

        mRecyclerView = findViewById(R.id.search_recycler)
        userRef = FirebaseDatabase.getInstance().reference.child("User")
        searchArray = ArrayList<SearchData>()

        handleIntent(intent)


    }

    override fun onSupportNavigateUp(): Boolean {
        val intent = Intent(applicationContext, MainActivity::class.java)
        startActivityForResult(intent, 0)
        return true
    }

    private fun handleIntent(intent: Intent){

        if (Intent.ACTION_SEARCH == intent.action){
 //           val query = intent.getStringExtra(SearchManager.QUERY)

            intent.getStringExtra(SearchManager.QUERY)?.also {query ->
                SearchRecentSuggestions(this, SearchSuggestion.AUTHORITY, SearchSuggestion.MODE)
                    .saveRecentQuery(query, null)
                searchForUsers(query)
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent?.let { handleIntent(it) }
    }



    fun toast(message: String?) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
       // return super.onOptionsItemSelected(item)
        when (item.itemId){
            R.id.action_sign_out -> {
                FirebaseAuth.getInstance().signOut()

                val intent = Intent(this@SearchActivity, LoginActivity2::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
                return  true
            }

        }
        return false
    }
    private fun searchForUsers(str:String?){
        val myList: ArrayList<SearchData> = ArrayList()

        if (userRef != null) {
            userRef.addValueEventListener(object : ValueEventListener {


                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        myList.clear()
                        searchArray.clear()
                        for (currentSnapshot in snapshot.children) {
                            val user =
                                currentSnapshot.child("advert").getValue(SearchData::class.java)

                                searchArray.add(user!!)

                        }

                    }
                    for(searchObj in searchArray){
                        if (searchObj.bus_name.toLowerCase().contains(str!!.toLowerCase())){
                            myList.add(searchObj)
                        }
                    }


                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })

        }
        Log.v("searchhh", myList.toString())
        Log.v("searchhh", searchArray.size.toString())

        mRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            val itemsDecoration = ItemDecoration(2)
            addItemDecoration(itemsDecoration)
            searchRecyclerAdapter = SearchRecyclerAdapter2(myList)
            mRecyclerView.adapter = searchRecyclerAdapter
        }

    }
    override fun onStart() {
        super.onStart()

    }

    override fun onStop() {
        super.onStop()
      //  searchRecyclerAdapter?.stopListening()
    }
    private fun loadFirebaseData(searchText: String?){
        toast(searchText)
        val query = userRef.orderByChild("category").equalTo(searchText)//.startAt("%${searchText}%").endAt(searchText + "\uf8ff")

        if (searchText!!.isEmpty()){
            toast("Search is empty")
        }else {
            val options: FirebaseRecyclerOptions<SearchData> =
                FirebaseRecyclerOptions.Builder<SearchData>()
                    .setQuery(query, object : SnapshotParser<SearchData> {

                        override fun parseSnapshot(snapshot: DataSnapshot): SearchData {

                            val searchData = SearchData(
                                snapshot.child("advert/uid").value.toString(),
                                snapshot.child("advert/purl").value.toString(),
                                snapshot.child("advert/bus_name").value.toString(),
                                snapshot.child("advert/category").value.toString()
                            )
                            Log.v("searchdebu", searchText)
                            Log.v("searchdebu", snapshot.value.toString())
                            Log.v("searchdebu", snapshot.child("advert/purl").value.toString())
                            Log.v("search", snapshot.child("advert/bus_name").value.toString())
                            Log.v("search", snapshot.child("advert/category").value.toString())

                            return searchData
                        }

                    })
                    .build()

            mRecyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                val itemsDecoration = ItemDecoration(5)
                addItemDecoration(itemsDecoration)
               // searchRecyclerAdapter = SearchRecyclerAdapter2(options)
                mRecyclerView.adapter = searchRecyclerAdapter
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.search_menu, menu)
        menuInflater.inflate(R.menu.main, menu)
        val searchMenu: MenuItem? = menu.findItem(R.id.search)


        if (searchMenu != null){
            searchView = MenuItemCompat.getActionView(searchMenu) as SearchView

            searchView.setOnCloseListener(object : SearchView.OnCloseListener{
                override fun onClose(): Boolean {
                    return true
                }
            })

            val searchPlate =  searchView.findViewById(androidx.appcompat.R.id.search_src_text) as EditText
            searchPlate.hint = "Search Artisan"

            val searchPlateView: View = searchView.findViewById(androidx.appcompat.R.id.search_plate)
            searchPlateView.setBackgroundColor(
                ContextCompat.getColor(this, android.R.color.transparent)
            )
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(query: String?): Boolean {
                   // Toast.makeText(applicationContext, query+2, Toast.LENGTH_SHORT).show()

                     searchForUsers(query)

                    return false
                }

                override fun onQueryTextChange(query: String?): Boolean {
                    return false
                }
            })
            //Associate searchable config

            val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
            //(menu.findItem(R.id.search).actionView as SearchView).apply {

            //  setSearchableInfo(searchManager.getSearchableInfo(componentName))
            //setIconifiedByDefault(false)
            searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))

        }
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

}