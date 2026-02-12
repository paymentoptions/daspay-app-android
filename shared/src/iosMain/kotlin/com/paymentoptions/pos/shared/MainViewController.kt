@file:OptIn(ExperimentalForeignApi::class, ExperimentalForeignApi::class)

package com.paymentoptions.pos.shared

import platform.UIKit.UIViewController
import platform.UIKit.UILabel
import platform.UIKit.UIColor
import platform.CoreGraphics.CGRectMake
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
fun MainViewController(): UIViewController {
    return object : UIViewController(nibName = null, bundle = null) {
        override fun viewDidLoad() {
            super.viewDidLoad()
            view.backgroundColor = UIColor.whiteColor

            val label = UILabel(frame = CGRectMake(0.0, 0.0, 300.0, 50.0))
            label.text = Greeting().greet()
            label.textColor = UIColor.blackColor
            label.center = view.center
            label.textAlignment = 1L // NSTextAlignmentCenter
            view.addSubview(label)
        }
    }
}

