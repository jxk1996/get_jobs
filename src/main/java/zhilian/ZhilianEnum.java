package zhilian;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

public class ZhilianEnum {

    @Getter
    public enum CityCode {
        NULL("不限", "0"),
        BEIJING("北京", "530"),
        SHANGHAI("上海", "538"),
        GUANGZHOU("广州", "763"),
        SHENZHEN("深圳", "765"),
        HANGZHOU("杭州", "653"),
        CHENGDU("成都", "801");

        private final String name;
        private final String code;

        CityCode(String name, String code) {
            this.name = name;
            this.code = code;
        }

        @JsonCreator
        public static CityCode forValue(String value) {
            for (CityCode cityCode : CityCode.values()) {
                if (cityCode.name.equals(value)) {
                    return cityCode;
                }
            }
            return NULL;
        }
    }

}
