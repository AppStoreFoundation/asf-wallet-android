package com.asfoundation.wallet.ui.balance

import android.Manifest
import android.animation.Animator
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract.EXTRA_INITIAL_URI
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import androidx.core.app.ActivityCompat
import com.asf.wallet.R
import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.router.TransactionsRouter
import com.asfoundation.wallet.ui.BaseActivity
import com.google.android.material.snackbar.Snackbar
import dagger.android.AndroidInjection
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.remove_wallet_activity_layout.*
import kotlinx.android.synthetic.main.restore_wallet_layout.*
import javax.inject.Inject


class RestoreWalletActivity : BaseActivity(), RestoreWalletActivityView {

  companion object {
    private const val RC_READ_EXTERNAL_PERMISSION_CODE = 1002
    private const val FILE_INTENT_CODE = 1003

    @JvmStatic
    fun newIntent(context: Context) = Intent(context, RestoreWalletActivity::class.java)
  }

  private lateinit var presenter: RestoreWalletActivityPresenter
  private var fileChosenSubject: PublishSubject<Uri>? = null
  private var onPermissionSubject: PublishSubject<Unit>? = null

  @Inject
  lateinit var walletsEventSender: WalletsEventSender

  override fun onCreate(savedInstanceState: Bundle?) {
    AndroidInjection.inject(this)
    super.onCreate(savedInstanceState)
    fileChosenSubject = PublishSubject.create()
    onPermissionSubject = PublishSubject.create()
    presenter = RestoreWalletActivityPresenter(walletsEventSender)
    setContentView(R.layout.restore_wallet_layout)
    toolbar()
    if (savedInstanceState == null) navigateToInitialRestoreFragment()
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      presenter.sendBackEvent()
      if (wallet_remove_animation == null || wallet_remove_animation.visibility != View.VISIBLE) super.onBackPressed()
      return true
    }
    return super.onOptionsItemSelected(item)
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == FILE_INTENT_CODE && resultCode == Activity.RESULT_OK && data != null) {
      val fileUri = data.data ?: Uri.parse("")
      fileChosenSubject?.onNext(fileUri)
    }
  }

  override fun onBackPressed() {
    if (wallet_remove_animation == null || wallet_remove_animation.visibility != View.VISIBLE) super.onBackPressed()
  }

  override fun navigateToPasswordView(keystore: String) {
    presenter.currentFragment = RestoreWalletPasswordFragment::class.java.simpleName
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, RestoreWalletPasswordFragment.newInstance(keystore))
        .addToBackStack(RestoreWalletPasswordFragment::class.java.simpleName)
        .commit()
  }

  override fun showWalletRestoreAnimation() {
    import_wallet_animation_group.visibility = View.VISIBLE
    background.visibility = View.VISIBLE
    background.animation = AnimationUtils.loadAnimation(this, R.anim.fast_fade_in_animation)
    import_wallet_animation.playAnimation()
  }

  override fun showWalletRestoredAnimation() {
    import_wallet_animation.setAnimation(R.raw.success_animation)
    import_wallet_text.text = getText(R.string.provide_wallet_created_header)
    import_wallet_text.visibility = View.VISIBLE
    import_wallet_animation.addAnimatorListener(object : Animator.AnimatorListener {
      override fun onAnimationRepeat(animation: Animator?) = Unit
      override fun onAnimationEnd(animation: Animator?) = navigateToTransactions()
      override fun onAnimationCancel(animation: Animator?) = Unit
      override fun onAnimationStart(animation: Animator?) = Unit
    })
    import_wallet_animation.repeatCount = 0
    import_wallet_animation.playAnimation()
  }

  override fun launchFileIntent(path: Uri?) {
    val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
      type = "*/*"
      path?.let {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) putExtra(EXTRA_INITIAL_URI, it)
      }
    }
    try {
      startActivityForResult(Intent.createChooser(intent, getString(R.string.import_wallet_title)),
          FILE_INTENT_CODE)
    } catch (ex: ActivityNotFoundException) {
      Snackbar.make(main_view, R.string.unknown_error, Snackbar.LENGTH_SHORT)
          .show()
    }
  }

  override fun hideAnimation() {
    import_wallet_animation.cancelAnimation()
    import_wallet_animation_group.visibility = View.GONE
  }

  override fun onFileChosen() = fileChosenSubject!!

  override fun askForReadPermissions() {
    if (ActivityCompat.checkSelfPermission(this,
            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
      onPermissionSubject?.onNext(Unit)
    } else {
      requestStorageReadPermission()
    }
  }

  override fun hideKeyboard() {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    imm?.hideSoftInputFromWindow(main_view.windowToken, 0)
  }

  private fun requestStorageReadPermission() {
    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
        RC_READ_EXTERNAL_PERMISSION_CODE)
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                          grantResults: IntArray) {
    if (requestCode == RC_READ_EXTERNAL_PERMISSION_CODE) {
      if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        onPermissionSubject?.onNext(Unit)
      }
    } else {
      super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
  }

  override fun onPermissionsGiven() = onPermissionSubject!!

  override fun onDestroy() {
    onPermissionSubject = null
    fileChosenSubject = null
    super.onDestroy()
  }

  private fun navigateToTransactions() {
    TransactionsRouter().open(this, true)
    finish()
  }

  private fun navigateToInitialRestoreFragment() {
    presenter.currentFragment = RestoreWalletFragment::class.java.simpleName
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, RestoreWalletFragment.newInstance())
        .commit()
  }
}
