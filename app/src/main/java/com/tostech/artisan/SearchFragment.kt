package com.tostech.artisan

import androidx.fragment.app.Fragment

class SearchFragment : Fragment() {
/*

    private var advertAdapter: AdvertAdapter? = null
    private var mUsers: List<AdvertData>? = null
    private lateinit var binding: FragmentSearchBinding
    lateinit var intent: Intent
    private var recyclerView: RecyclerView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false)
        mUsers = ArrayList()
        retrieveAllUsers()

        handleIntent(intent)

        recyclerView = binding.searchRecycler

        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.layoutManager = LinearLayoutManager(context)

        return binding.root
    }

    private fun retrieveAllUsers() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser!!.uid

        val usersRef = FirebaseDatabase.getInstance().reference

        usersRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                (mUsers as ArrayList<UserData>).clear()
                for (currentSnapshot in snapshot.children){
                    val  user: UserData? = snapshot.getValue(UserData::class.java)

                    if (!(user!!.id).equals(firebaseUser)){
                        (mUsers as ArrayList<UserData>).add(user)
                    }
                }

                advertAdapter = AdvertAdapter(context!!, mUsers!!, false)
                recyclerView!!.adapter = advertAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    override fun onStart() {
        super.onStart()
         intent = requireActivity().intent
        if (intent != null)
            handleIntent(intent)
    }

    private fun handleIntent(intent: Intent){

        if (Intent.ACTION_SEARCH == intent.action){
            val query = intent.getStringExtra(SearchManager.QUERY)

            searchForUsers(query)
        }
    }

    private fun searchForUsers(str:String?){
        val firebaseUser = FirebaseAuth.getInstance().currentUser!!.uid

        val queryUsers = FirebaseDatabase.getInstance().reference.child("advert").orderByChild("bus_name").startAt(str)
            .endAt(str +"\uf8ff")
        queryUsers.addValueEventListener(object: ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                (mUsers as ArrayList<UserData>).clear()
                for (currentSnapshot in snapshot.children){
                    val  user: UserData? = snapshot.getValue(UserData::class.java)

                    if (!(user!!.id).equals(firebaseUser)){
                        (mUsers as ArrayList<UserData>).add(user)
                    }
                }

                advertAdapter = AdvertAdapter(context!!, mUsers!!, false)
                recyclerView!!.adapter = advertAdapter

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
    }*/
}