package com.backend.stockAllocation.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewRequest {
    private boolean approved;
    private String reviewedBy;
    private String note;
}

