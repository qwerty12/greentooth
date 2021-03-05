# <img src="fastlane/metadata/android/en-US/images/icon.png" width="24"> Greentooth

__PLEASE NOTE THAT THIS APP IS NO LONGER MAINTAINED AND MAY NOT WORK ON NEWER ANDROID VERSIONS__

This app disables Bluetooth automatically when the last Bluetooth device has
been disconnected.

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
  alt="Get it on F-Droid"
  height="80">](https://f-droid.org/en/packages/com.smilla.greentooth)
  

Bluetooth can cause unnecessary battery drain and is a potential security risk if
it is always turned on. Security experts recommend [switching Bluetooth off](https://www.webroot.com/us/en/resources/tips-articles/a-review-of-bluetooth-attacks-and-how-to-secure-mobile-workforce-devices) 
 [when you're not using it](https://www.wired.com/story/turn-off-bluetooth-security/).
 However, manually disabling Bluetooth when you're done listening to music or
 talking in your headset is annoying and easy to forget.

Greentooth can help you save battery and mitigate the security risks of Bluetooth
by automatically turning Bluetooth off when it is no longer needed. When the
last Bluetooth device has disconnected Greentooth will wait for an adjustable
amount of time in order to not disturb any connection/reconnection attempts and
then deactivate Bluetooth.

Please note that Greentooth only runs upon device disconnection, it does not do
periodical checks for idle Bluetooth.  
<br/> 

<div>
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/1.png" width="300">
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/2.png" width="300">
</div>

## Frequently Asked Questions
**Could you make a similar app but for Wi-Fi?**

Several people have suggested an app that works like Greentooth but for Wi-Fi, 
but unfortunately, it's not possible due to technical reasons.  

  Google doesn't want developers running their apps in the background so they
changed the rules with later Android versions to make it more difficult.
Some exceptions were left for things like Bluetooth, which is why Greentooth is
able to work like it does. However, any app that checks for Wi-Fi usage in a
similar way would have to be running all the time in the foreground. Obviously
this is contraproductive to the whole battery saving aspect. It would also have
to constantly show an annoying notification to let the user know it's running.  

  For these reasons I won't be making an app to turn off Wi-Fi,
at least not unless Google makes it possible to do it well.

**Greentooth isn't working, what should I do?**
1. Make sure that the app is enabled and that a reasonable delay is set.
2. Make sure that your Bluetooth device is actually disconnecting when you think it is and that there are no other Bluetooth devices left connected to your phone or tablet.
3. Your phone might be preventing Greentooth from launching when a Bluetooth device disconnect is detected. Try whitelisting the app/adding it to the "protected" list in your battery saver settings. Make sure it is allowed to launch from the background. You can find guides on how to do this for many vendors at https://dontkillmyapp.com/.

**I have a Huawei device and the app keeps asking for permission to turn off Bluetooth. What should I do?**

This is an issue with Huawei's own Android customization EMUI and it affects all Bluetooth automation apps, see [issue 6](https://www.gitlab.com/nbergman/greentooth/issues/6). Unfortunately, there doesn't seem to be a way around it at this time.
