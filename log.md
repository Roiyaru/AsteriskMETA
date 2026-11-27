# run 46063031664 sha 1ff3fc8061c11a5175a013ad3166a62e88372085

## kernel.log (tail 400)
```
   Compiling secrecy v0.10.3
   Compiling serde_path_to_error v0.1.20
   Compiling prometheus-client-derive-encode v0.4.2
   Compiling io_tee v0.1.1
   Compiling strsim v0.11.1
   Compiling linux-raw-sys v0.12.1
   Compiling dtoa v1.0.11
   Compiling matchit v0.8.4
   Compiling meow-listener v0.30.0 (/home/runner/work/_temp/kernel/rust/.upstream/meow/crates/meow-listener)
   Compiling base64 v0.21.7
   Compiling http-range-header v0.4.2
   Compiling tower-http v0.6.11
   Compiling age-core v0.11.0
   Compiling axum v0.8.9
   Compiling i18n-embed-fl v0.9.4
   Compiling scrypt v0.11.0
   Compiling meow-config v0.30.0 (/home/runner/work/_temp/kernel/rust/.upstream/meow/crates/meow-config)
   Compiling libclash-rs v0.30.0-android.1 (/home/runner/work/_temp/kernel/rust)
   Compiling sysinfo v0.32.1
warning: meow-lwip@0.30.0: host os android
   Compiling cesu8 v1.1.0
   Compiling bech32 v0.9.1
   Compiling fastrand v2.5.0
   Compiling unsafe-libyaml v0.2.11
   Compiling serde_yaml v0.9.34+deprecated
   Compiling tempfile v3.27.0
   Compiling age v0.11.5
   Compiling jni v0.21.1
   Compiling meow-api v0.30.0 (/home/runner/work/_temp/kernel/rust/.upstream/meow/crates/meow-api)
warning: `meow-proxy` (lib) generated 2 warnings
    Finished `release` profile [optimized] target(s) in 6m 23s
warning: the following packages contain code that will be rejected by a future version of Rust: proc-macro-error2 v2.0.1
note: to see what the problems were, use the option `--future-incompat-report`, or run `cargo report future-incompatibilities --id 1`
+ /usr/local/lib/android/sdk/ndk/30.0.16248370/toolchains/llvm/prebuilt/linux-x86_64/bin/clang --target=aarch64-linux-android26 -O2 -fPIE -pie -Wall -Wextra -Werror -Wl,-z,max-page-size=16384 -Wl,--build-id=none -Wl,-s -o /tmp/clash-rs-3plez183/jni/arm64-v8a/libmeow.so /home/runner/work/_temp/kernel/rust/native/launcher.c -ldl
warning: [options] source value 8 is obsolete and will be removed in a future release
warning: [options] target value 8 is obsolete and will be removed in a future release
warning: [options] To suppress warnings about obsolete options, use -Xlint:-options.
3 warnings
Built /home/runner/work/_temp/libclash-rs.aar
+ cd rust/.upstream/meow
+ cargo test -p meow-config --lib
info: syncing channel updates for 1.98.1-x86_64-unknown-linux-gnu
info: latest update on 2026-09-03 for version 1.98.1 (48a229cea 2026-09-01)
info: component rustfmt is up to date
info: downloading component clippy
    Updating crates.io index
 Downloading crates ...
  Downloaded icu_properties v2.3.0
  Downloaded form_urlencoded v1.2.3
  Downloaded h2 v0.4.18
  Downloaded ed25519 v2.2.3
  Downloaded const-oid v0.9.6
  Downloaded log v0.4.34
  Downloaded icu_provider v2.3.1
  Downloaded memmap2 v0.9.11
  Downloaded sealed v0.6.0
  Downloaded cc v1.9.0
  Downloaded ctutils v0.4.5
  Downloaded icu_collections v2.3.0
  Downloaded pkcs8 v0.10.2
  Downloaded der v0.8.2
  Downloaded icu_locale_core v2.3.0
  Downloaded icu_normalizer_data v2.3.0
  Downloaded itertools v0.10.5
  Downloaded der v0.7.10
  Downloaded icu_properties_data v2.3.0
  Downloaded boring v5.2.0
  Downloaded ring-compat v0.8.0
  Downloaded encoding_rs v0.8.42
  Downloaded icu_normalizer v2.3.0
  Downloaded signature v2.2.0
  Downloaded shadowsocks-crypto v0.6.2
  Downloaded spki v0.7.3
  Downloaded spin v0.10.1
  Downloaded smallvec v1.16.3
  Downloaded tinystr v0.8.4
  Downloaded tokio-boring v5.2.0
  Downloaded shadowsocks v1.24.0
  Downloaded yoke v0.8.3
  Downloaded utf8_iter v1.0.4
  Downloaded url v2.5.9
  Downloaded webpki-root-certs v1.0.10
  Downloaded unicode-width v0.2.2
  Downloaded boring-sys v5.2.0
  Downloaded zerovec-derive v0.11.6
  Downloaded zerovec v0.11.8
  Downloaded camellia v0.1.0
   Compiling libc v0.2.191
   Compiling cfg-if v1.0.5
   Compiling proc-macro2 v1.0.107
   Compiling quote v1.0.47
   Compiling unicode-ident v1.0.27
   Compiling typenum v1.20.1
   Compiling version_check v0.9.5
   Compiling serde_core v1.0.229
   Compiling syn v3.0.9
   Compiling serde v1.0.229
   Compiling generic-array v0.14.7
   Compiling syn v2.0.119
   Compiling getrandom v0.2.17
   Compiling jobserver v0.1.35
   Compiling find-msvc-tools v0.1.15
   Compiling shlex v2.0.1
   Compiling cc v1.9.0
   Compiling rand_core v0.6.4
   Compiling crypto-common v0.1.7
   Compiling getrandom v0.4.3
   Compiling rand_core v0.10.1
   Compiling cmov v0.5.4
   Compiling subtle v2.6.1
   Compiling ctutils v0.4.5
   Compiling scopeguard v1.2.0
   Compiling hybrid-array v0.4.15
   Compiling portable-atomic v1.15.0
   Compiling pin-project-lite v0.2.17
   Compiling memchr v2.8.3
   Compiling crypto-common v0.2.2
   Compiling synstructure v0.14.0
   Compiling cpufeatures v0.2.17
   Compiling critical-section v1.2.0
   Compiling parking_lot_core v0.9.12
   Compiling once_cell v1.21.4
   Compiling serde_derive v1.0.229
   Compiling zerofrom-derive v0.1.8
   Compiling lock_api v0.4.14
   Compiling bytes v1.12.1
   Compiling yoke-derive v0.8.4
   Compiling stable_deref_trait v1.2.1
   Compiling block-buffer v0.10.4
   Compiling futures-sink v0.3.34
   Compiling futures-core v0.3.34
   Compiling digest v0.10.7
   Compiling zerovec-derive v0.11.6
   Compiling zerofrom v0.1.8
   Compiling errno v0.3.14
   Compiling yoke v0.8.3
   Compiling glob v0.3.4
   Compiling futures-io v0.3.34
   Compiling slab v0.4.12
   Compiling clang-sys v1.9.1
   Compiling signal-hook-registry v1.4.8
   Compiling displaydoc v0.2.7
   Compiling tokio-macros v2.7.2
   Compiling zerovec v0.11.8
   Compiling mio v1.2.4
   Compiling socket2 v0.6.5
   Compiling pkg-config v0.3.34
   Compiling futures-channel v0.3.34
   Compiling futures-macro v0.3.34
   Compiling inout v0.2.5
   Compiling futures-task v0.3.34
   Compiling itoa v1.0.18
   Compiling log v0.4.34
   Compiling smallvec v1.16.3
   Compiling regex-syntax v0.8.11
   Compiling minimal-lexical v0.2.1
   Compiling cpufeatures v0.3.1
   Compiling nom v7.1.3
   Compiling futures-util v0.3.34
   Compiling parking_lot v0.12.5
   Compiling tokio v1.54.1
   Compiling regex-automata v0.4.18
   Compiling block-buffer v0.12.1
   Compiling inout v0.1.4
   Compiling libloading v0.8.9
   Compiling thiserror v2.0.21
   Compiling bindgen v0.72.1
   Compiling zeroize v1.9.1
   Compiling zerocopy v0.8.63
   Compiling getrandom v0.3.4
   Compiling either v1.19.0
   Compiling cipher v0.4.4
   Compiling itertools v0.10.5
   Compiling regex v1.13.1
   Compiling cipher v0.5.2
   Compiling cexpr v0.6.0
   Compiling tinystr v0.8.4
   Compiling tracing-core v0.1.36
   Compiling thiserror-impl v2.0.21
   Compiling writeable v0.6.4
   Compiling bitflags v2.13.2
   Compiling rustc-hash v2.1.3
   Compiling litemap v0.8.3
   Compiling shlex v1.3.0
   Compiling icu_locale_core v2.3.0
   Compiling futures-executor v0.3.34
   Compiling potential_utf v0.1.6
   Compiling zerotrie v0.2.5
   Compiling universal-hash v0.5.1
   Compiling cmake v0.1.58
   Compiling fslock v0.2.1
   Compiling opaque-debug v0.3.1
   Compiling icu_properties_data v2.3.0
   Compiling utf8_iter v1.0.4
   Compiling icu_normalizer_data v2.3.0
   Compiling fs_extra v1.3.0
   Compiling zmij v1.0.23
   Compiling icu_collections v2.3.0
   Compiling icu_provider v2.3.1
   Compiling futures v0.3.34
   Compiling rand_core v0.9.5
   Compiling tracing-attributes v0.1.31
   Compiling httparse v1.10.1
   Compiling pin-project-internal v1.1.13
   Compiling tracing v0.1.44
   Compiling aead v0.5.2
   Compiling ahash v0.7.8
   Compiling vcpkg v0.2.15
   Compiling semver v1.0.28
   Compiling ppv-lite86 v0.2.21
   Compiling serde_json v1.0.154
   Compiling libsqlite3-sys v0.39.1
   Compiling rustc_version v0.4.1
   Compiling rand_chacha v0.9.0
   Compiling pin-project v1.1.13
   Compiling icu_normalizer v2.3.0
   Compiling icu_properties v2.3.0
   Compiling boring-sys v5.2.0
   Compiling rand v0.9.5
   Compiling polyval v0.6.2
   Compiling hmac v0.12.1
   Compiling sha1 v0.10.7
   Compiling foreign-types-macros v0.2.4
   Compiling blake3 v1.8.7
   Compiling rustversion v1.0.23
   Compiling byteorder v1.5.0
   Compiling autocfg v1.5.1
   Compiling cpubits v0.1.1
   Compiling percent-encoding v2.3.2
   Compiling foldhash v0.2.0
   Compiling foreign-types-shared v0.3.1
   Compiling libm v0.2.16
   Compiling foreign-types v0.5.0
   Compiling num-traits v0.2.19
   Compiling hashbrown v0.17.1
   Compiling form_urlencoded v1.2.3
   Compiling idna_adapter v1.2.2
   Compiling ghash v0.5.1
   Compiling curve25519-dalek v4.1.3
   Compiling poly1305 v0.8.0
   Compiling ctr v0.9.2
   Compiling aes v0.8.4
   Compiling chacha20 v0.9.1
   Compiling http v1.5.0
   Compiling zstd-sys v2.1.2+zstd.1.5.7
   Compiling openssl-macros v0.1.1
   Compiling universal-hash v0.6.1
   Compiling aho-corasick v1.1.5
   Compiling ring v0.17.14
   Compiling data-encoding v2.11.1
   Compiling arrayvec v0.7.8
   Compiling constant_time_eq v0.4.2
   Compiling snap v1.1.2
   Compiling polyval v0.7.3
   Compiling chacha20poly1305 v0.10.1
   Compiling aes-gcm v0.10.3
   Compiling hashbrown v0.12.3
   Compiling idna v1.1.0
   Compiling ipnet v2.12.2
   Compiling async-trait v0.1.92
   Compiling curve25519-dalek-derive v0.1.1
   Compiling socket2 v0.5.10
   Compiling crossbeam-utils v0.8.23
   Compiling powerfmt v0.2.1
   Compiling reed-solomon-erasure v6.0.0
   Compiling deranged v0.5.9
   Compiling num-conv v0.2.2
   Compiling fallible-iterator v0.3.0
   Compiling untrusted v0.9.0
   Compiling equivalent v1.0.3
   Compiling fallible-streaming-iterator v0.1.9
   Compiling time-core v0.1.9
   Compiling crc32fast v1.5.3
   Compiling rusqlite v0.41.1
   Compiling time v0.3.55
   Compiling indexmap v2.14.2
   Compiling encoding_rs v0.8.42
   Compiling url v2.5.9
   Compiling lru v0.7.8
   Compiling ghash v0.6.0
   Compiling tungstenite v0.29.0
   Compiling aes v0.9.3
   Compiling tokio-util v0.7.20
   Compiling ctr v0.10.1
   Compiling chacha20 v0.10.2
   Compiling aead v0.6.1
   Compiling digest v0.11.3
   Compiling smol_str v0.3.7
   Compiling memmap2 v0.9.11
   Compiling keccak v0.2.2
   Compiling zstd-safe v8.2.0
   Compiling spin v0.9.9
   Compiling rustls-pki-types v1.15.1
   Compiling atomic-waker v1.1.2
   Compiling sponge-cursor v0.1.0
   Compiling ipnetwork v0.21.1
   Compiling fnv v1.0.7
   Compiling base64 v0.22.1
   Compiling h2 v0.4.18
   Compiling maxminddb v0.27.3
   Compiling webpki-root-certs v1.0.10
   Compiling meow-common v0.30.0 (/home/runner/work/_temp/kernel/rust/.upstream/meow/crates/meow-common)
   Compiling rand v0.10.3
   Compiling aes-gcm v0.11.1
   Compiling tokio-tungstenite v0.29.0
   Compiling tokio-snappy v0.2.0
   Compiling ring-compat v0.8.0
   Compiling x25519-dalek v2.0.1
   Compiling blowfish v0.10.0
   Compiling camellia v0.1.0
   Compiling hkdf v0.12.4
   Compiling pbkdf2 v0.12.2
   Compiling salsa20 v0.11.0
   Compiling twofish v0.8.0
   Compiling cast5 v0.12.0
   Compiling des v0.9.0
   Compiling md-5 v0.10.6
   Compiling sha2 v0.10.9
   Compiling dynosaur_derive v0.3.1
   Compiling base64ct v1.8.3
   Compiling nohash-hasher v0.2.0
   Compiling meow-trie v0.30.0 (/home/runner/work/_temp/kernel/rust/.upstream/meow/crates/meow-trie)
   Compiling quiche v0.30.0
   Compiling ryu v1.0.23
   Compiling multiversion_no_op v1.0.0
   Compiling rustix v1.1.6
   Compiling tinyvec v1.13.3
   Compiling simdutf8 v0.1.5
   Compiling hashbrown v0.14.5
   Compiling core_detect v1.0.0
   Compiling dashmap v6.2.1
   Compiling hickory-proto v0.26.3
   Compiling enum_dispatch v0.3.13
   Compiling serde_urlencoded v0.7.1
   Compiling dynosaur v0.3.1
   Compiling password-hash v0.5.0
   Compiling shadowsocks-crypto v0.6.2
   Compiling sha3 v0.12.0
   Compiling shake v0.1.0
   Compiling module-lattice v0.2.3
   Compiling lru v0.18.5
   Compiling tokio-tfo v0.4.3
   Compiling sendfd v0.4.5
   Compiling blake2 v0.10.6
   Compiling spin v0.10.1
   Compiling sealed v0.6.0
   Compiling trait-variant v0.1.3
   Compiling kem v0.3.0
   Compiling intrusive-collections v0.10.3
   Compiling web-time v1.1.0
   Compiling unicode-width v0.2.2
   Compiling static_assertions v1.1.0
   Compiling linux-raw-sys v0.12.1
   Compiling anstyle v1.0.14
   Compiling byte_string v1.0.0
   Compiling bit-vec v0.8.0
   Compiling lru_time_cache v0.11.11
   Compiling arraydeque v0.5.1
   Compiling anyhow v1.0.104
   Compiling debug_panic v0.2.1
   Compiling lazy_static v1.5.1
   Compiling octets v0.3.7
   Compiling sharded-slab v0.1.7
   Compiling granit-parser v2.0.0
   Compiling bit-set v0.8.0
   Compiling shadowsocks v1.24.0
   Compiling annotate-snippets v0.12.17
   Compiling yamux v0.14.1
   Compiling ml-kem v0.3.3
   Compiling argon2 v0.5.3
   Compiling zstd v0.14.3
   Compiling encoding_rs_io v0.1.8
   Compiling matchers v0.2.0
   Compiling tracing-log v0.2.0
   Compiling tracing-serde v0.2.0
   Compiling thread_local v1.1.10
   Compiling nu-ansi-term v0.50.3
   Compiling base64 v0.23.1
   Compiling fastrand v2.5.0
   Compiling hex v0.4.3
   Compiling tempfile v3.27.0
   Compiling serde-saphyr v2.0.0
   Compiling tracing-subscriber v0.3.23
   Compiling meow-rules v0.30.0 (/home/runner/work/_temp/kernel/rust/.upstream/meow/crates/meow-rules)
   Compiling fancy-regex v0.16.2
   Compiling boring v5.2.0
   Compiling tokio-boring v5.2.0
   Compiling meow-transport v0.30.0 (/home/runner/work/_temp/kernel/rust/.upstream/meow/crates/meow-transport)
   Compiling meow-dns v0.30.0 (/home/runner/work/_temp/kernel/rust/.upstream/meow/crates/meow-dns)
error[E0560]: struct `DnsClient` has no field named `doh_h2`
   --> crates/meow-dns/src/client.rs:781:13
    |
781 |             doh_h2: parking_lot::Mutex::default(),
    |             ^^^^^^ `DnsClient` does not have this field
    |
    = note: all struct fields are already assigned

For more information about this error, try `rustc --explain E0560`.
error: could not compile `meow-dns` (lib) due to 1 previous error
warning: build failed, waiting for other jobs to finish...
```

## build.log (tail 120)
```
```
