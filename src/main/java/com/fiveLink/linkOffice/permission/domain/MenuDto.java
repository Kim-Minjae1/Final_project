package com.fiveLink.linkOffice.permission.domain;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class MenuDto {
    private Long menu_no;
    private String menu_name;
    private LocalDateTime menu_create_date;
    private LocalDateTime menu_update_date;

    public Menu toEntity() {
        return Menu.builder()
                .menuNo(menu_no)
                .menuName(menu_name)
                .menuCreateDate(menu_create_date)
                .menuUpdateDate(menu_update_date)
                .build();
    }

    public static MenuDto toDto(Menu menu) {
        return MenuDto.builder()
                .menu_no(menu.getMenuNo())
                .menu_name(menu.getMenuName())
                .menu_create_date(menu.getMenuCreateDate())
                .menu_update_date(menu.getMenuUpdateDate())
                .build();
    }
}
