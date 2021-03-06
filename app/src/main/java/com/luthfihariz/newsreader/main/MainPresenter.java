package com.luthfihariz.newsreader.main;

import com.luthfihariz.newsreader.data.Article;
import com.luthfihariz.newsreader.data.source.NewsRepository;
import com.luthfihariz.newsreader.util.schedulers.BaseSchedulerProvider;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by luthfihariz on 5/20/17.
 */

class MainPresenter implements MainContract.Presenter {

    private NewsRepository mRepository;
    private MainContract.View mView;
    private BaseSchedulerProvider mScheduler;
    private List<Article> mArticles;

    MainPresenter(NewsRepository repository, BaseSchedulerProvider schedulerProvider) {
        mRepository = repository;
        mScheduler = schedulerProvider;
        mArticles = new ArrayList<>();
    }

    @Override
    public void bind(MainContract.View view) {
        mView = view;
        mView.setPresenter(this);
    }

    @Override
    public void unbind() {
        mView.setPresenter(null);
        mView = null;
    }

    @Override
    public void getArticles() {
        mView.showProgress();
        mRepository.getArticles()
                .subscribeOn(mScheduler.io())
                .observeOn(mScheduler.ui())
                .doOnNext(this::showArticles)
                .doOnError(this::showErrorView)
                .subscribe();
    }

    @Override
    public void isUserPickAnySource() {
        mView.showProgress();
        mRepository.isSelectedSourceEmpty()
                .subscribeOn(mScheduler.io())
                .observeOn(mScheduler.ui())
                .doOnNext(this::goToSourcePicker)
                .doOnError(this::showErrorView)
                .subscribe();
    }

    private void goToSourcePicker(Boolean isEmpty) {
        if (mView != null) {
            if (isEmpty) {
                mView.goToSourcePicker();
            } else {
                getArticles();
            }
        }
    }

    private void showArticles(List<Article> articles) {
        if (mView != null) {
            mView.hideProgress();
            mArticles.addAll(0, articles);
            mView.showArticles(mArticles);
        }
    }

    private void showErrorView(Throwable e) {
        if (mView != null) {
            mView.hideProgress();
            mView.showErrorView();
        }
    }
}