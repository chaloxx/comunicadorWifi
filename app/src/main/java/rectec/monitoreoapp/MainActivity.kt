package rectec.monitoreoapp

import android.annotation.SuppressLint
import android.content.Intent

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_main.*
import rectec.monitoreoapp.adapters.PagerAdapter
import rectec.monitoreoapp.util.AlertDialogManager


class MainActivity() : AppCompatActivity(), Profiles.Notifier {

    private lateinit var home : Home
    private lateinit var events : Events
    private val init = { _ : Boolean, _ : String -> initFragments()}

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AlertDialogManager(this,init).buildForLogin()
    }


    override fun onResume() {
        super.onResume()
        AlertDialogManager(this,init).buildForLogin()
    }


    fun initFragments(){
        Intent(this,MessageService::class.java). also {
            startService(it)
        }

        supportActionBar?.hide()
        //create  adapter and add fragments
        val pagerAdapter = PagerAdapter(supportFragmentManager).apply {
            home = Home()
            events = Events()
            addFragment(home,"Home")
            addFragment(events,"Eventos")
            addFragment(Profiles(this@MainActivity),"Pérfiles")
            addFragment(Information(),"Información")
        }
        //set adapter
        viewPager.adapter = pagerAdapter
        //unabled swipe between primary fragments
        viewPager.setSwipePagingEnabled(false)

        // add adapter to tablelayout
        val tab = findViewById<TabLayout>(R.id.tabLayout)
        tab.setupWithViewPager(viewPager)

    }


    override fun notifyToTheFragments() {
        home.updateView()
        events.updateView()
    }




}