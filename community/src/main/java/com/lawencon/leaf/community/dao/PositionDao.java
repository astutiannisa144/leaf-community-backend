package com.lawencon.leaf.community.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.lawencon.base.ConnHandler;
import com.lawencon.leaf.community.model.Position;

@Repository
public class PositionDao extends BaseDao<Position> {

	@SuppressWarnings("unchecked")
	@Override
	public List<Position> getAll() {
		final StringBuilder str = new StringBuilder();
		str.append("SELECT * ");
		str.append("FROM t_position");

		final List<Position> activities = ConnHandler.getManager().createNativeQuery(str.toString(), Position.class)
				.getResultList();
		return activities;
	}

	@Override
	public Optional<Position> getByIdAndDetach(String id) {
		return Optional.ofNullable(super.getByIdAndDetach(Position.class, id));
	}

	@Override
	public Optional<Position> getById(String id) {

		return Optional.ofNullable(super.getById(Position.class, id));

	}

}
