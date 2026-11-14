-allowaccessmodification
-repackageclasses

# AndroidLibClashLite supplies its gomobile JNI keep rules in the AAR.

# aircompressor reaches sun.misc.Unsafe only for its direct-buffer path, which the MRS decoder
# never uses; keep R8 from failing on a class it cannot resolve from the Android platform stubs.
-dontwarn sun.misc.**
