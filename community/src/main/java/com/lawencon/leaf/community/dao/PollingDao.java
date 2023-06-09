package com.lawencon.leaf.community.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.lawencon.leaf.community.model.Polling;

@Repository
public class PollingDao extends BaseDao<Polling> {

	@Override
	List<Polling> getAll() {
		return null;
	}

	@Override
	public Optional<Polling> getById(String id) {
		return Optional.ofNullable(super.getById(Polling.class, id));
	}

	@Override
	Optional<Polling> getByIdAndDetach(String id) {
		return Optional.empty();
	}

}
