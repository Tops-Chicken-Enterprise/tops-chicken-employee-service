package com.topschicken.employeeservice.dto.response.paginate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponseDto<T> {
    private List<T> items;
    private PageMetaDataDto meta;

    public static <T> PagedResponseDto<T> of(List<T> items, PageMetaDataDto meta) {
        return PagedResponseDto.<T>builder()
                .items(items)
                .meta(meta)
                .build();
    }
}