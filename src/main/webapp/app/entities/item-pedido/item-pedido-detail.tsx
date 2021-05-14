import React, { useEffect } from 'react';
import { connect } from 'react-redux';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { IRootState } from 'app/shared/reducers';
import { getEntity } from './item-pedido.reducer';
import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';

export interface IItemPedidoDetailProps extends StateProps, DispatchProps, RouteComponentProps<{ id: string }> {}

export const ItemPedidoDetail = (props: IItemPedidoDetailProps) => {
  useEffect(() => {
    props.getEntity(props.match.params.id);
  }, []);

  const { itemPedidoEntity } = props;
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="itemPedidoDetailsHeading">
          <Translate contentKey="qrdapioApp.itemPedido.detail.title">ItemPedido</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{itemPedidoEntity.id}</dd>
          <dt>
            <span id="quantidade">
              <Translate contentKey="qrdapioApp.itemPedido.quantidade">Quantidade</Translate>
            </span>
          </dt>
          <dd>{itemPedidoEntity.quantidade}</dd>
          <dt>
            <Translate contentKey="qrdapioApp.itemPedido.item">Item</Translate>
          </dt>
          <dd>{itemPedidoEntity.item ? itemPedidoEntity.item.id : ''}</dd>
          <dt>
            <Translate contentKey="qrdapioApp.itemPedido.pedido">Pedido</Translate>
          </dt>
          <dd>{itemPedidoEntity.pedido ? itemPedidoEntity.pedido.id : ''}</dd>
        </dl>
        <Button tag={Link} to="/item-pedido" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/item-pedido/${itemPedidoEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

const mapStateToProps = ({ itemPedido }: IRootState) => ({
  itemPedidoEntity: itemPedido.entity,
});

const mapDispatchToProps = { getEntity };

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(mapStateToProps, mapDispatchToProps)(ItemPedidoDetail);
