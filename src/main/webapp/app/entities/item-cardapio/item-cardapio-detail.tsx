import React, { useEffect } from 'react';
import { connect } from 'react-redux';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { IRootState } from 'app/shared/reducers';
import { getEntity } from './item-cardapio.reducer';
import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';

export interface IItemCardapioDetailProps extends StateProps, DispatchProps, RouteComponentProps<{ id: string }> {}

export const ItemCardapioDetail = (props: IItemCardapioDetailProps) => {
  useEffect(() => {
    props.getEntity(props.match.params.id);
  }, []);

  const { itemCardapioEntity } = props;
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="itemCardapioDetailsHeading">
          <Translate contentKey="qrdapioApp.itemCardapio.detail.title">ItemCardapio</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{itemCardapioEntity.id}</dd>
          <dt>
            <span id="categoria">
              <Translate contentKey="qrdapioApp.itemCardapio.categoria">Categoria</Translate>
            </span>
          </dt>
          <dd>{itemCardapioEntity.categoria}</dd>
          <dt>
            <span id="nome">
              <Translate contentKey="qrdapioApp.itemCardapio.nome">Nome</Translate>
            </span>
          </dt>
          <dd>{itemCardapioEntity.nome}</dd>
          <dt>
            <span id="descricao">
              <Translate contentKey="qrdapioApp.itemCardapio.descricao">Descricao</Translate>
            </span>
          </dt>
          <dd>{itemCardapioEntity.descricao}</dd>
          <dt>
            <span id="valor">
              <Translate contentKey="qrdapioApp.itemCardapio.valor">Valor</Translate>
            </span>
          </dt>
          <dd>{itemCardapioEntity.valor}</dd>
          <dt>
            <Translate contentKey="qrdapioApp.itemCardapio.cardapio">Cardapio</Translate>
          </dt>
          <dd>{itemCardapioEntity.cardapio ? itemCardapioEntity.cardapio.id : ''}</dd>
        </dl>
        <Button tag={Link} to="/item-cardapio" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/item-cardapio/${itemCardapioEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

const mapStateToProps = ({ itemCardapio }: IRootState) => ({
  itemCardapioEntity: itemCardapio.entity,
});

const mapDispatchToProps = { getEntity };

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(mapStateToProps, mapDispatchToProps)(ItemCardapioDetail);
