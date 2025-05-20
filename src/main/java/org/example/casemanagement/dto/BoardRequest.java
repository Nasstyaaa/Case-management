package org.example.casemanagement.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardRequest {
    @NotBlank(message = "Board name is required")
    private String name;
    
    private String description;
    
    @Builder.Default
    private boolean public_ = false;

    public boolean isPublic() {
        return public_;
    }

    public void setPublic(boolean public_) {
        this.public_ = public_;
    }
} 