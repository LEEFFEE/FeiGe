# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\develop\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
#自己编写
#-ignorewarnings
-optimizationpasses 5       #指定代码的压缩级别 0 - 7
-dontusemixedcaseclassnames     #是否使用大小写混合
-dontskipnonpubliclibraryclasses    #如果应用程序引入的有jar包，并且想混淆jar包里面的class
-dontpreverify  #混淆时是否做预校验（可去掉加快混淆速度）
-verbose        #混淆时是否记录日志（混淆后生产映射文件 map 类名 -> 转化后类名的映射
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*     #混淆采用的算法

##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.leeffee.feige.ui.cloud.entity.** {*;}
-keepattributes EnclosingMethod     # 反射
##---------------End: proguard configuration for Gson  ---------

#-keep public class * extends android.app.Activity
#-keep public class * extends android.app.Application
#-keep public class * extends android.app.Service
#-keep public class * extends android.content.BroadcastReceiver
#-keep public class * extends android.content.ContentProvider
#-keep public class * extends android.app.backup.BackupAgentHelper
#-keep public class * extends android.preference.Preference
#-keep public class com.android.vending.licensing.ILicensingService
-keepclasseswithmembernames class * {   #保护指定的类和类的成员的名称，如果所有指定的类成员出席（在压缩步骤之后）
    native <methods>;       #保持 native 的方法不去混淆
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);   #保护指定的类和类的成员，但条件是所有指定的类和类成员是要存在。
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int); #保持自定义控件类不被混淆，指定格式的构造方法不去混淆
}

-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);    #保持指定规则的方法不被混淆（Android layout 布局文件中为控件配置的onClick方法不能混淆）
}
-keep public class * extends android.view.View {    #保持自定义控件指定规则的方法不被混淆
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}
-keepclassmembers enum * {  #保持枚举 enum 不被混淆
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;#保持 Parcelable 不被混淆（aidl文件不能去混淆）
}
-keepnames class * implements java.io.Serializable #需要序列化和反序列化的类不能被混淆（注：Java反射用到的类也不能被混淆）

-dontwarn android.widget.**     #如果有警告也不终止
-keep class android.widget.** {*;}      #保护指定的类文件和类的成员
-dontwarn android.support.**
-keep class android.support.**{*;}
-dontwarn com.google.**
-keep class com.google.** { *; }


-dontwarn butterknife.**
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }

-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-dontwarn com.bumptech.glide.**
-keep class com.bumptech.glide.** { *; }
-dontwarn org.hamcrest.**
-keep class org.hamcrest.** { *; }
-dontwarn com.squareup.**
-keep class com.squareup.** { *; }
-dontwarn javax.annotation.**
-keep class javax.annotation.** { *; }
-dontwarn javax.inject.**
-keep class javax.inject.** { *; }
-dontwarn org.junit.**
-keep class org.junit.** { *; }
-dontwarn uk.co.senab.photoview.**
-keep class uk.co.senab.photoview.** { *; }
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-dontwarn okio.**
-keep class okio.** { *; }
-dontwarn org.reactivestreams.**
-keep class org.reactivestreams.** { *; }
-dontwarn io.reactivex.**
-keep class io.reactivex.** { *; }
-dontwarn com.readystatesoftware.systembartint.**
-keep class com.readystatesoftware.systembartint.** { *; }
-dontwarn com.jcodecraeer.xrecyclerview.**
-keep class com.jcodecraeer.xrecyclerview.** { *; }

-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}

-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}
-dontshrink     #不压缩输入的类文件
-dontoptimize   #不优化输入的类文件
-keepattributes Exceptions,InnerClasses,Signature    #过滤异常、内部类、泛型（不写可能会出现类型转换错误，一般情况把这个加上就是了）
-keepattributes *Annotation*    #假如项目中有用到注解，应加入这行配置
-keepattributes SourceFile,LineNumberTable  # 保留行号
-keep class **.R$* { *; }  #保持R文件不被混淆，否则，你的反射是获取不到资源id的
