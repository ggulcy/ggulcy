package spamdetector.zani.presenter

interface BasePresenter<T> {

    fun takeView(view: T)
    fun dropView()

}