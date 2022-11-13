package com.example.todo

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import java.util.*

class MainActivity : AppCompatActivity() {

    private val realm: Realm by lazy {
            Realm.getDefaultInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val taskList = readAll()

        // タスクリストが空だったときにダミーデータを生成する
        if (taskList.isEmpty()) {
            createDummyData()
        }

        val adapter = TaskAdapter(this, taskList, object : TaskAdapter.OnItemClickListener {
            override fun onItemClick(item: Task) {
                // クリック時の処理
                Toast.makeText(applicationContext, item.content + "を削除しました", Toast.LENGTH_SHORT).show()
                //item.deleteFromRealm()
                delete(item)
            }
        }, true)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    fun createDummyData() {
        for (i in 0..10) {
            create(R.drawable.ic_launcher_background, "やること $i")
        }
    }

    fun create(imageId: Int, content: String) {
        realm.executeTransactionAsync {
            val task = it.createObject(Task::class.java, UUID.randomUUID().toString())
            task.imageId = imageId
            task.content = content
            it.copyFromRealm(task)
        }
    }

    fun readAll(): RealmResults<Task> {
        return realm.where(Task::class.java).findAll().sort("createdAt", Sort.ASCENDING)
    }

    fun update(id: String, content: String) {
        realm.executeTransactionAsync {
            val task = realm.where(Task::class.java).equalTo("id", id).findFirst()
                ?: return@executeTransactionAsync
            task.content = content
        }
    }

    fun update(task: Task, content: String) {
        realm.executeTransactionAsync {
            task.content = content
        }
    }

    fun delete(id: String) {
        realm.executeTransactionAsync {
            val task = realm.where(Task::class.java).equalTo("id", id).findFirst()
                ?: return@executeTransactionAsync
            task.deleteFromRealm()
        }
    }

    fun delete(task: Task) {
        realm.executeTransactionAsync {
            task.deleteFromRealm()
            realm.close()
        }
    }

    fun deleteAll() {
        realm.executeTransactionAsync {
            realm.deleteAll()
        }
    }

}