package com.parkez.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Schema(description = "오너 소셜 회원가입 추가입력 요청")
public class SocialOwnerProfileCompleteRequest {

	@NotBlank(message = "전화번호는 필수 입력 항목입니다.")
	@Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "전화번호 양식에 맞지 않습니다. ex) 010-1234-5678")
	@Schema(description = "전화번호", example = "010-1234-5678")
	private String phone;

	@NotBlank(message = "사업자등록번호는 필수 입력 항목입니다.123123123123")
	@Schema(description = "사업자등록번호", example = "123-45-67890")
	@Pattern(regexp = "^\\d{3}-\\d{2}-\\d{5}$", message = "사업자등록번호 양식에 맞지 않습니다. ex) 123-45-67890")
	private String businessNumber;

	@NotBlank(message = "은행명 필수 입력 항목입니다.")
	@Schema(description = "은행명", example = "신한은행")
	private String bankName;

	@NotBlank(message = "계좌번호는 필수 입력 항목입니다.")
	@Schema(description = "계좌번호", example = "110-1234-5678")
	@Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "계좌번호 양식에 맞지 않습니다. ex) 110-1234-5678")
	private String bankAccount;

	@NotBlank(message = "예금주명은 필수 입력 항목입니다.")
	@Schema(description = "예금주명", example = "홍길동")
	private String depositorName;


}
