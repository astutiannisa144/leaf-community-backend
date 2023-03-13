package com.lawencon.leaf.community.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lawencon.base.ConnHandler;
import com.lawencon.leaf.community.constant.EnumRole;
import com.lawencon.leaf.community.dao.UserDao;
import com.lawencon.leaf.community.dao.VerificationDao;
import com.lawencon.leaf.community.model.User;
import com.lawencon.leaf.community.model.Verification;
import com.lawencon.leaf.community.pojo.PojoRes;
import com.lawencon.leaf.community.pojo.verification.PojoVerificationReq;
import com.lawencon.leaf.community.pojo.verification.PojoVerificationRes;
import com.lawencon.leaf.community.util.GenerateCodeUtil;

@Service
public class VerificationService extends BaseService<PojoVerificationRes> {

	@Autowired
	private VerificationDao verificationDao;
	@Autowired
	private EmailSenderService emailSenderService;
	@Autowired
	private UserDao userDao;

	@Override
	public PojoVerificationRes getById(String id) {
		final PojoVerificationRes pojoVerificationRes = new PojoVerificationRes();
		Verification verification = verificationDao.getById(id).get();
		pojoVerificationRes.setVerificationCode(verification.getVerificationCode());
		pojoVerificationRes.setEmail(verification.getEmail());
		pojoVerificationRes.setId(id);
		pojoVerificationRes.setVer(verification.getVer());

		return pojoVerificationRes;
	}

	@Override
	public List<PojoVerificationRes> getAll() {
		final List<PojoVerificationRes> pojoVerificationRes = new ArrayList<>();
		List<Verification> verifications = verificationDao.getAll();
		for (int i = 0; i < verifications.size(); i++) {
			PojoVerificationRes verification = new PojoVerificationRes();
			verification.setVerificationCode(verifications.get(i).getVerificationCode());
			verification.setEmail(verification.getEmail());
			verification.setId(verifications.get(i).getId());
			verification.setVer(verifications.get(i).getVer());
			pojoVerificationRes.add(verification);
		}
		return pojoVerificationRes;
	}

	public PojoRes insert(PojoVerificationReq data) {
		ConnHandler.begin();
		final User system = userDao.getUserByRole(EnumRole.SY.getCode()).get();

		Verification verification = new Verification();

		verification.setEmail(data.getEmail());
		verification.setExpiredTime(LocalDateTime.now().plusMinutes(5));
		verification.setVerificationCode(GenerateCodeUtil.generateNumber(6));
		verification.setIsActive(true);
		verificationDao.saveNoLogin(verification, () -> system.getId());
		ConnHandler.commit();

		final PojoRes pojoRes = new PojoRes();
		pojoRes.setMessage("Code sent to your email");

		new Thread(() -> emailSenderService.sendMail(data.getEmail(), "Verify your email address",
				"Dear," + data.getEmail() + "\n" + "To verify your email address, enter this code in your browser : "
						+ verification.getVerificationCode() + "\n" + "Thank you"))
				.start();

		return pojoRes;
	}

	public PojoRes delete(String id) {

		try {
			ConnHandler.begin();
			verificationDao.deleteById(Verification.class, id);
		} catch (Exception e) {
			e.printStackTrace();

		}
		final PojoRes pojoRes = new PojoRes();
		pojoRes.setMessage("Verification Deleted");
		return pojoRes;
	}

}
