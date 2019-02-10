package com.seungleekim.android.movie.ui.details

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.seungleekim.android.movie.R
import com.seungleekim.android.movie.di.ActivityScoped
import com.seungleekim.android.movie.model.*
import com.seungleekim.android.movie.ui.MovieDetailsActivity
import com.seungleekim.android.movie.ui.details.review.MovieReviewsAdapter
import com.seungleekim.android.movie.ui.details.review.ReviewDialogFragment
import com.seungleekim.android.movie.ui.details.trailer.MovieTrailersAdapter
import com.seungleekim.android.movie.util.GlideApp
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_details.*
import kotlinx.android.synthetic.main.view_movie_details_top.*
import kotlinx.android.synthetic.main.view_movie_details_trailers.*
import kotlinx.android.synthetic.main.view_movie_details_summary_reviews.*
import javax.inject.Inject

@ActivityScoped
class MovieDetailsFragment @Inject constructor() : DaggerFragment(), MovieDetailsContract.View,
    MovieTrailersAdapter.OnClickListener, MovieReviewsAdapter.OnClickListener {

    @Inject
    lateinit var mPresenter: MovieDetailsContract.Presenter

    private var mMovie: Movie? = null
    private var mMovieDetails: MovieDetails? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMovie = arguments?.getParcelable(ARG_MOVIE)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startLoadingAnimation()
        mPresenter.takeView(this)
        mPresenter.getMovieDetails(mMovie!!)
        mPresenter.getFavorite(mMovie!!)
        setupToolbar()
        setupFavoriteFab()
    }

    private fun startLoadingAnimation() {
        lav_movie_details_loading.playAnimation()
    }

    private fun setupToolbar() {
        (activity as MovieDetailsActivity).apply {
            setSupportActionBar(toolbar_details)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun setupFavoriteFab() {
        fab_movie_details_favorite.setOnClickListener {
            mPresenter.onFavoriteFabClick(mMovie!!)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.dropView()
    }

    override fun showMovieDetails(movieDetails: MovieDetails) {
        mMovieDetails = movieDetails
        showMovieTitle(movieDetails.getTitleWithYear())
        showMovieBackdrop(movieDetails.getBackdropUrl())
        showMovieRating(movieDetails.getRatingText())
        showMovieMpaaRating(movieDetails.mpaaRating)
        showMovieDuration(movieDetails.getRuntimeString())
        showMovieGenres(movieDetails.getFirstGenre())
        showMovieReleaseDate(movieDetails.releaseDate)
        showMovieTrailers(movieDetails.trailers)
        showMovieOverview(movieDetails.overview)
        showMovieCasts(movieDetails.getCastsString())
        showMovieCrews(movieDetails.getCrewsString())
        showMovieReviews(movieDetails.reviews)
        hideView(container_movie_details_loading)
        showView(container_movie_details)
    }

    override fun showMovieTitle(title: String) {
        collapsing_toolbar.title = title
        tv_movie_details_title.text = title
    }

    override fun showMovieBackdrop(backdropUrl: String) {
        GlideApp.with(context!!).load(backdropUrl).into(iv_details_movie_backdrop)
    }

    override fun showMovieRating(rating: String) {
        tv_movie_details_rating.text = rating
    }

    override fun showMovieMpaaRating(mpaaRating: String?) {
        tv_movie_details_mpaa_rating.text = mpaaRating
    }

    @SuppressLint("SetTextI18n")
    override fun showMovieDuration(runtime: String) {
        tv_movie_details_runtime.text = runtime
    }

    override fun showMovieGenres(genres: String) {
        tv_movie_details_genres.text = genres
    }

    override fun showMovieReleaseDate(releaseDate: String) {
        tv_movie_details_release_date.text = releaseDate
    }

    override fun showFavorite(setFavorite: Boolean, showAnimation: Boolean) {
        if (setFavorite) {
            if (showAnimation) {
                showView(lav_favorited)
                lav_favorited.playAnimation()
                lav_favorited.addAnimatorListener(object: Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator?) {
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        hideView(lav_favorited)
                    }

                    override fun onAnimationCancel(animation: Animator?) {
                    }

                    override fun onAnimationStart(animation: Animator?) {
                    }

                })
            }
            Snackbar.make(container_movie_details, "Added ${mMovie!!.title} to favorite movies",
                Snackbar.LENGTH_SHORT).show()
            fab_movie_details_favorite.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ic_favorite_full))
        } else {
            Snackbar.make(container_movie_details, "Removed ${mMovie!!.title} from favorite movies",
                Snackbar.LENGTH_SHORT).show()
            fab_movie_details_favorite.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ic_favorite_empty))
        }
    }

    override fun showMovieTrailers(trailers: List<Trailer>) {
        if (trailers.isEmpty()) {
            return
        }
        showView(container_view_movie_details_trailers)
        rv_movie_details_trailers.setHasFixedSize(true)
        rv_movie_details_trailers.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rv_movie_details_trailers.adapter = MovieTrailersAdapter(this)
        (rv_movie_details_trailers.adapter as MovieTrailersAdapter).submitList(trailers)
    }

    override fun onTrailerClick(trailer: Trailer) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(trailer.getYoutubeUrl()))
        startActivity(intent)
    }

    override fun showMovieOverview(overview: String?) {
        if (overview == null) {
            return
        }
        showView(container_movie_details_overview)
        tv_movie_details_overview_content.text = overview
    }

    override fun showMovieCasts(casts: String?) {
        if (casts == null) {
            return
        }
        showView(container_movie_details_casts)
        tv_movie_details_casts_content.text = casts

    }
    override fun showMovieCrews(crews: String?) {
        if (crews == null) {
            return
        }
        showView(container_movie_details_crews)
        tv_movie_details_featured_crews_content.text = crews
    }

    override fun showMovieReviews(reviews: List<Review>) {
        if (reviews.isEmpty()) {
            return
        }
        showView(container_movie_details_reviews)
        rv_movie_details_reviews.setHasFixedSize(true)
        rv_movie_details_reviews.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rv_movie_details_reviews.adapter = MovieReviewsAdapter(this)
        (rv_movie_details_reviews.adapter as MovieReviewsAdapter).submitList(reviews)
    }

    override fun onReviewClick(review: Review) {
        val dialog = ReviewDialogFragment.newInstance(review)
        dialog.show(activity?.supportFragmentManager!!, "review_dialog")
    }

    override fun showFailureMessage() {
        Toast.makeText(context, "DB Transaction Failed", Toast.LENGTH_LONG).show()
    }

    companion object {
        private const val ARG_MOVIE = "movie"

        fun newInstance(movie: Movie): MovieDetailsFragment {
            val fragment = MovieDetailsFragment()
            val args = Bundle()
            args.putParcelable(ARG_MOVIE, movie)
            fragment.arguments = args
            return fragment
        }
    }
}
