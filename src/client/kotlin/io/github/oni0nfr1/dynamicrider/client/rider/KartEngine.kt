package io.github.oni0nfr1.dynamicrider.client.rider

enum class KartEngine(val engineCode: Int, val isDummy: Boolean, val engineName: String) {
    // 공식 엔진
    PRO(18, false, "pro"),
    NEW(13, false, "new"),
    JIU(12, false, "jiu"),
    X(10, false, "x"),
    V1(15, false, "v1"),
    EX(11, false, "ex"),
    LEGACY(17, false, "1.0"),
    Z7(14, false, "z7"),
    A2(16, false, "a2"),
    RUSHPLUS(19, false, "rush+"),
    CHARGE(20, false, "charge"),

    // 더미 엔진
    N1(1000, true, "n1"),
    KEY(1002, true, "key"),
    MK(1003, true, "mk"),
    BOAT(1004, true, "boat"),
    GEAR(1005, true, "gear"),
    F1(1006, true, "f1"),
    RALLY(1007, true, "rally");

    companion object {
        private val byEngineCode = entries.associateBy { it.engineCode }

        fun getByCode(engineCode: Int): KartEngine? {
            return byEngineCode[engineCode]
        }
    }
}