package edu.androidgroupproject

import android.graphics.Canvas
import android.view.SurfaceView

class EntityManager private constructor() {
    private val entityList: MutableList<EntityBase> = mutableListOf()
    private val addQueue: MutableList<EntityBase> = mutableListOf()
    private var view: SurfaceView? = null

    fun Init(_view: SurfaceView?) {
        view = _view
    }

    fun Update(_dt: Float) {
        // Add newly created entities
        entityList.addAll(addQueue)
        addQueue.clear() // Clean up add queue

        val removalList = mutableListOf<EntityBase>()

        // Update all entities and collect those that should be removed
        for (currEntity in entityList) {
            currEntity.Update(_dt)
            if (currEntity.IsDone()) removalList.add(currEntity)
        }

        // Collision Check
        for (i in entityList.indices) {
            val first = entityList[i]
            if (first is Collidable) {
                for (j in i + 1 until entityList.size) {
                    val second = entityList[j]
                    if (second is Collidable && Collision.AABBToAABB(first, second)) {
                        first.OnHit(second)
                        second.OnHit(first)
                    }
                }
            }
        }

        // Remove entities that are done
        entityList.removeAll(removalList)
    }

    fun EmptyEntityList() {
        entityList.clear()
        addQueue.clear()
    }

    fun Render(_canvas: Canvas) {
        // Sort entities by render layer before rendering
        entityList.sortedBy { it.GetRenderLayer() }.forEach { it.Render(_canvas) }
    }

    fun AddEntity(_newEntity: EntityBase) {
        view?.let {
            _newEntity.Init(it)
            addQueue.add(_newEntity)
        } ?: run {
            // Log warning if `view` is null (Prevents crashes)
            println("EntityManager: Attempted to add entity but view is null.")
        }
    }

    fun ContainsEntity(entityType: Class<out EntityBase>): Boolean {
        return entityList.any { it.javaClass == entityType }
    }

    companion object {
        val Instance: EntityManager = EntityManager()
    }
}
