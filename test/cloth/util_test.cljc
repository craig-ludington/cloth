(ns cloth.util-test
  (:require [cloth.util :as util]
    #?@(:cljs [[cljs.test :refer-macros [is are deftest testing use-fixtures]]]
        :clj  [[clojure.test :refer [is are deftest testing use-fixtures]]])))

(deftest test-add0x
  (is (= (util/add0x "ab0c") "0xab0c"))
  (is (= (util/add0x "0xab0c") "0xab0c")))

(deftest test-strip0x
  (is (= (util/strip0x "ab0c") "ab0c"))
  (is (= (util/strip0x "0xab0c") "ab0c")))

(deftest test-sha3
  (is (= (util/->hex (util/sha3 "hello")) "1c8aff950685c2ed4bc3174f3472287b56d9517b9c948127319a09a7a36deac8")))

(deftest test-hex->int
  (is (= (util/hex->int "0x0") 0))
  (is (= (util/hex->int "0x00") 0))
  (is (= (util/hex->int "0xff00") 65280))
  (is (= (util/hex->int "00") 0))
  (is (= (util/hex->int "ff00") 65280)))

(deftest test-int->hex
  (is (= (util/int->hex 0) "00"))
  (is (= (util/int->hex 65280) "ff00")))

(deftest test-rpad
  (is (= (util/->hex (util/rpad (util/hex-> "ab") 4)) "ab000000"))
  (is (= (util/->hex (util/rpad (util/hex-> "ab001010") 4)) "ab001010"))
  (is (= (util/->hex (util/rpad (util/hex-> "ab001010") 2)) "1010")))

(deftest test-pad
  (is (= (util/->hex (util/pad (util/hex-> "ab") 4)) "000000ab"))
  (is (= (util/->hex (util/pad (util/hex-> "ab001010") 4)) "ab001010"))
  (is (= (util/->hex (util/pad (util/hex-> "ab001010") 2)) "1010")))

(deftest encode-solidity-tests
  (is (= (util/encode-solidity :bool true) "0000000000000000000000000000000000000000000000000000000000000001"))
  (is (= (util/encode-solidity :bool false) "0000000000000000000000000000000000000000000000000000000000000000"))

  (is (= (util/encode-solidity :uint8 1) "0000000000000000000000000000000000000000000000000000000000000001"))
  (is (= (util/encode-solidity :uint8 0) "0000000000000000000000000000000000000000000000000000000000000000"))

  (is (= (util/encode-solidity :uint32 1) "0000000000000000000000000000000000000000000000000000000000000001"))
  (is (= (util/encode-solidity :uint32 0) "0000000000000000000000000000000000000000000000000000000000000000"))

  (is (= (util/encode-solidity "uint32[2]" [1 15]) "0000000000000000000000000000000000000000000000000000000000000001000000000000000000000000000000000000000000000000000000000000000f"))
  (is (= (util/encode-solidity "uint32[]" [1 15]) "00000000000000000000000000000000000000000000000000000000000000020000000000000000000000000000000000000000000000000000000000000001000000000000000000000000000000000000000000000000000000000000000f"))


  (is (= (util/encode-solidity :string "hello") "000000000000000000000000000000000000000000000000000000000000000568656c6c6f000000000000000000000000000000000000000000000000000000"))
  (is (= (util/encode-solidity :string " ") "00000000000000000000000000000000000000000000000000000000000000012000000000000000000000000000000000000000000000000000000000000000"))

  )

(deftest decode-solidity-tests
  (is (= (util/decode-solidity :bool "0000000000000000000000000000000000000000000000000000000000000001") true ))
  (is (= (util/decode-solidity :bool  "0000000000000000000000000000000000000000000000000000000000000000") false ))
  (is (= (util/decode-solidity :uint8 "0000000000000000000000000000000000000000000000000000000000000001") 1))
  (is (= (util/decode-solidity :uint8 "0000000000000000000000000000000000000000000000000000000000000000") 0))
  (is (= (util/decode-solidity :uint32 "0000000000000000000000000000000000000000000000000000000000000001") 1))
  (is (= (util/decode-solidity :uint32 "0000000000000000000000000000000000000000000000000000000000000000") 0))
  (is (= (util/decode-solidity :string "000000000000000000000000000000000000000000000000000000000000000568656c6c6f000000000000000000000000000000000000000000000000000000")
         "hello"))
  (is (= (util/decode-solidity :string "00000000000000000000000000000000000000000000000000000000000000012000000000000000000000000000000000000000000000000000000000000000") " " ))

  )

(deftest dynamic-type-tests
  (is (util/dynamic-type? :bytes))
  (is (util/dynamic-type? :string))
  (is (util/dynamic-type? "uint32[]"))
  (is (not (util/dynamic-type? "uint32[3]")))
  (is (not (util/dynamic-type? :bytes32)))
  (is (not (util/dynamic-type? :uint256)))
  )
(deftest encode-fn-sig-tests
  ; Eamples from https://github.com/ethereum/wiki/wiki/Ethereum-Contract-ABI
  (is (= (util/encode-fn-sig "baz" [:uint32 :bool] [69 true])                         ;baz(uint32 x, bool y)
         "0xcdcd77c000000000000000000000000000000000000000000000000000000000000000450000000000000000000000000000000000000000000000000000000000000001"))

  ;(is (= (util/encode-fn-sig "bar" ["fixed128x128[2]"] [[2.125, 8.5]])                         ;bar(fixed[2] xy)
  ;       "0xab55044d00000000000000000000000000000002200000000000000000000000000000000000000000000000000000000000000880000000000000000000000000000000"))

  (is (= (util/encode-fn-sig "sam" [:bytes :bool "uint256[]"] ["dave" true [1,2,3]])                         ;sam(bytes name, bool z, uint[] data)
         "0xa5643bf20000000000000000000000000000000000000000000000000000000000000060000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000a0000000000000000000000000000000000000000000000000000000000000000464617665000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000003000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000020000000000000000000000000000000000000000000000000000000000000003"))

  (is (= (util/encode-fn-sig "f" [:uint256 "uint32[]" :bytes10 :bytes] [291M [1110M 1929M] "1234567890" "Hello, world!"])                         ;f(uint,uint32[],bytes10,bytes)
         "0x8be6524600000000000000000000000000000000000000000000000000000000000001230000000000000000000000000000000000000000000000000000000000000080313233343536373839300000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000e0000000000000000000000000000000000000000000000000000000000000000200000000000000000000000000000000000000000000000000000000000004560000000000000000000000000000000000000000000000000000000000000789000000000000000000000000000000000000000000000000000000000000000d48656c6c6f2c20776f726c642100000000000000000000000000000000000000"))

  (is (= (util/encode-fn-sig "setMessage" [:string] ["Hello"])                         ;setMessage(string)
         "0x368b87720000000000000000000000000000000000000000000000000000000000000020000000000000000000000000000000000000000000000000000000000000000548656c6c6f000000000000000000000000000000000000000000000000000000")))
