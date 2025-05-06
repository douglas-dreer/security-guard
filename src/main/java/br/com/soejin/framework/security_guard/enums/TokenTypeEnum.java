package br.com.soejin.framework.security_guard.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TokenTypeEnum {
    BEARER("Bearer"),
    REFRESH("Refresh");
    private final String type;
}
