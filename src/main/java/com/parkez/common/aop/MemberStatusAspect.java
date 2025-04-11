package com.parkez.common.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.parkez.common.exception.CommonErrorCode;
import com.parkez.common.exception.ParkingEasyException;
import com.parkez.common.principal.AuthUser;

@Aspect
@Component
public class MemberStatusAspect {

	@Before("@within(com.parkez.common.aop.CheckMemberStatus) || @annotation(com.parkez.common.aop.CheckMemberStatus)")
	public void checkMemberStatus() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof AuthUser authUser)) {
			throw new ParkingEasyException(CommonErrorCode.UNAUTHORIZED);
		}

		if (!authUser.isSignupCompleted()) {
			throw new ParkingEasyException(CommonErrorCode.MEMBER_INFO_NOT_COMPLETED);
		}
	}
}
